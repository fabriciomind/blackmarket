package net.thirdy.blackmarket.controls;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.controlsfx.control.GridCell;
import org.elasticsearch.common.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jexiletools.es.model.Currencies;
import io.jexiletools.es.model.Mod;
import io.jexiletools.es.model.Price;
import io.jexiletools.es.model.json.ExileToolsHit;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.thirdy.blackmarket.ex.BlackmarketException;
import net.thirdy.blackmarket.util.ImageCache;
import net.thirdy.blackmarket.util.SwingUtil;

public class ItemGridCell extends GridCell<ExileToolsHit> {
	
	public static final Double DOUBLE_ZERO = Double.valueOf(0);

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private final List<Label> explicitModsLbls; 
	private final VBox modsPane = new VBox();
	private final ItemPropertiesGridPane propertiesGridPane = new ItemPropertiesGridPane();
	private final Label itemName = new Label("Item Name Here");
	private final HBox itemNameGraphics = new HBox(1);
	private final Label implicitMod = new Label("Implicit Mod Here");
	private final Separator implicitModSeparator = new Separator(Orientation.HORIZONTAL);
	private final Separator itemNameSeparator = new Separator(Orientation.HORIZONTAL);
	
	private final Hyperlink wtbBtn = new Hyperlink("Want to buy");
	private final Label priceLbl = new Label("");
	private final TextFlow playerInfo = new TextFlow();
	private final HBox bottomPane = new HBox(3);
	
	private final BorderPane borderPane = new BorderPane();

	StackPane stackPane = new StackPane();

	double scaledWidth = 170 ;
	double scaledHeight = 189 ;

	public ItemGridCell() {
		getStyleClass().add("image-grid-cell"); //$NON-NLS-1$
		propertiesGridPane.setId("propertiesGridPane");
		explicitModsLbls = Arrays.asList(
				new Label(),
				new Label(),
				new Label(),
				new Label(),
				new Label(),
				new Label(),
				new Label(),
				new Label(),
				new Label(),
				new Label(),
				new Label(),
				new Label(),
				new Label(),
				new Label()
				);
		explicitModsLbls.stream()
			.forEach(l -> {
				l.getStyleClass().add("explcit-mod");
				l.setWrapText(true);
			});
		implicitMod.getStyleClass().add("implicit-mod");
		implicitMod.setWrapText(true);
		itemName.getStyleClass().add("item-name");
		itemName.setWrapText(true);
		itemName.setGraphic(itemNameGraphics);
		itemName.setContentDisplay(ContentDisplay.LEFT);
		priceLbl.setTextFill(Color.WHITE);
		priceLbl.setContentDisplay(ContentDisplay.RIGHT);

		wtbBtn.setOnAction(e -> wtbHandler());
		wtbBtn.setStyle("-fx-font-size: 8pt;");
		stackPane.setStyle("-fx-background-color: rgba(30, 30, 30, 0.5); -fx-background-radius: 5; -fx-font-size: 8pt;");
		
		Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
		bottomPane.getChildren().addAll(priceLbl, playerInfo, spacer, wtbBtn);
		borderPane.setBottom(bottomPane);
		GridPane gridCenterPane = setupCenterGridPane();
		GridPane.setHalignment(modsPane, HPos.LEFT);
		gridCenterPane.add(modsPane, 0, 0);
		GridPane.setHalignment(propertiesGridPane, HPos.RIGHT);
		gridCenterPane.add(propertiesGridPane, 1, 0);
		borderPane.setCenter(gridCenterPane);
		borderPane.setPadding(new Insets(5, 0, 5, 5));
	}
	

	private void wtbHandler() {
		StringSelection stringSelection = new StringSelection(getItem().toWTB());
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
	}


	private GridPane setupCenterGridPane() {
		GridPane gridpane = new GridPane();
//		gridpane.setPadding(new Insets(5));
		gridpane.setHgap(2);
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(70);
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(30);
		column1.setHgrow(Priority.ALWAYS);
		column2.setHgrow(Priority.ALWAYS);
		gridpane.getColumnConstraints().addAll(column1, column2);
		return gridpane;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateItem(ExileToolsHit item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setGraphic(null);
		} else {
			try {
				setupItemUI(item);
			} catch (Exception e) {
				Dialogs.showExceptionDialog(e);
			}
			setGraphic(stackPane);
		}
	}


	private void setupItemUI(ExileToolsHit item) {
		ImageView imageView = new ImageView();
		imageView.setPreserveRatio(true);
		imageView.setSmooth(true);
		// imageView.setCache(true); // makes the image blurry
		imageView.setOpacity(0.5);
		
		// Setup image
		Image image = ImageCache.getInstance().get(item.getInfo().getIcon());
		imageView.setImage(image);

		if (image.getHeight() > scaledHeight) {
			imageView.setFitWidth(94);
			imageView.setFitHeight(scaledHeight);
		} else {
			imageView.setScaleX(0.6025641025641026);
			imageView.setScaleY(0.6025641025641026);
		}
		
		// Setup labels
		clearModLabels();
		setupPrice(item);
		setupItemName(item);
		setupPlayerInfo(item);
		item.getImplicitMod().ifPresent(m -> implicitMod.setText(m.toDisplay()));
		populateModLabels(item);
		updatePropertiesPane(item);
		
		modsPane.getChildren().clear();
		modsPane.getChildren().add(itemName);
		modsPane.getChildren().add(itemNameSeparator);
		item.getImplicitMod().ifPresent(m -> {
			modsPane.getChildren().add(implicitMod);
			modsPane.getChildren().add(implicitModSeparator);
		});
		
		modsPane.getChildren().addAll(
				explicitModsLbls.stream().filter(l -> StringUtils.isNotBlank(l.getText())).collect(Collectors.toList()));
		
		stackPane.getChildren().clear();
		stackPane.getChildren().addAll(imageView, borderPane);
	}

	private void updatePropertiesPane(ExileToolsHit item) {
		propertiesGridPane.getChildren().clear();
		
		Optional<String> socks = Optional.ofNullable(item.getSockets())
			.map(s -> s.getAllSockets());
		if(socks.isPresent()) propertiesGridPane.add("S:", socks.get());
		else propertiesGridPane.add("", "");
		
		item.getQuality().ifPresent(d -> {
			propertiesGridPane.add("Q:", d);
		});
		
		item.getTotalDPS().ifPresent(d -> {
			propertiesGridPane.add("DPS:", d);
		});
		
		item.getPhysicalDPS().ifPresent(d -> {
			propertiesGridPane.add("pDPS:", d);
		});
		
		item.getPhysicalDamage().ifPresent(d -> {
			propertiesGridPane.add("Phys:", d);
		});
		
		item.getElementalDPS().ifPresent(d -> {
			propertiesGridPane.add("eDPS:", d);
		});
		
		item.getElementalDamage().ifPresent(d -> {
			propertiesGridPane.add("Elem:", d);
		});
		
		item.getAPS().ifPresent(d -> {
			propertiesGridPane.add("APS:", d);
		});
		
		item.getCriticalStrikeChance().ifPresent(d -> {
			propertiesGridPane.add("Crit:", d);
		});
		
		item.getArmour().ifPresent(d -> {
			propertiesGridPane.add("Ar:", d);
		});
		
		item.getEvasionRating().ifPresent(d -> {
			propertiesGridPane.add("Ev:", d);
		});
		
		item.getEnergyShield().ifPresent(d -> {
			propertiesGridPane.add("Es:", d);
		});
		
		item.getChanceToBlock().ifPresent(d -> {
			propertiesGridPane.add("Blk:", d);
		});
		
	}

	private void setupPrice(ExileToolsHit item) {
		Optional<Price> price = item.getShop().getPrice();
		price.ifPresent(p -> {
			String amt = new DecimalFormat("#.##").format(p.getAmount());
			Image img = ImageCache.getInstance().get(p.getCurrency().icon().orElse(null));
			priceLbl.setText(amt + "x");
			ImageView curImgView = new ImageView(img);
			curImgView.setPreserveRatio(true);
			curImgView.setFitHeight(21);
			priceLbl.setGraphic(curImgView);
		});
	}

	private void setupPlayerInfo(ExileToolsHit item) {
		playerInfo.getChildren().clear();
		Optional<String> sellerIGN = Optional.ofNullable(StringUtils.trimToNull(item.getShop().getSellerIGN()));
		Optional<String> sellerAccount = Optional.ofNullable(StringUtils.trimToNull(item.getShop().getSellerAccount()));

		String shopUrl = "https://www.pathofexile.com/forum/view-thread/" + item.getShop().getThreadid();
		
		if (sellerIGN.isPresent()) {
			Text ignText = new Text("IGN: ");
			ignText.setFill(Color.WHITE);
			String ign = sellerIGN.get();
			ign = truncateIgn(ign);
			Hyperlink link = new Hyperlink(ign);
			link.setTextFill(Color.WHITE);
			link.setOnAction(e -> openLink(shopUrl));
			playerInfo.getChildren().addAll(
					ignText, link);
		} else if (sellerAccount.isPresent()) {
			Text sellerAccountText = new Text("Account: ");
			sellerAccountText.setFill(Color.WHITE);
			Hyperlink link = new Hyperlink(sellerAccount.get());
			link.setTextFill(Color.WHITE);
			link.setOnAction(e -> openLink(shopUrl));
			playerInfo.getChildren().addAll(
					sellerAccountText, link);
		} else {
			Text shopText = new Text("Shop: ");
			shopText.setFill(Color.WHITE);
			Hyperlink link = new Hyperlink(item.getShop().getThreadid());
			link.setTextFill(Color.WHITE);
			link.setOnAction(e -> openLink(shopUrl));
			playerInfo.getChildren().addAll(
					shopText, link);
		}
	}

	private String truncateIgn(String ign) {
		// sometimes we get a value like this:
		// i_am_ready_to_die/internecivus_lanius/Catarina_is_my_sister
		// https://www.pathofexile.com/forum/view-thread/1435290
		if (ign.matches(".+/.+")) {
			ign = ign.split("/")[0];
		} 
		return ign;
	}

	private void openLink(String shopUrl) {
		try {
			SwingUtil.openUrlViaBrowser(shopUrl);
		} catch (BlackmarketException e) {
			Dialogs.showExceptionDialog(e);
		}
	}

	private void setupItemName(ExileToolsHit item) {
		itemName.setText(item.getInfo().getFullName());
		itemName.setTextFill(Color.web(item.getAttributes().getRarityAsEnum().webColor()));
		itemNameGraphics.getChildren().clear();
		if (item.getAttributes().getCorrupted()) {
			Image image = ImageCache.getInstance().get(Currencies.vaal.icon().get());
			itemNameGraphics.getChildren().add(new ImageView(image));
		}
		if (item.getAttributes().getMirrored()) {
			Image image = ImageCache.getInstance().get(Currencies.mirror.icon().get());
			itemNameGraphics.getChildren().add(new ImageView(image));
		}
	}

	private void clearModLabels() {
		explicitModsLbls.stream()
			.forEach(l -> l.setText(""));
	}

	private void populateModLabels(ExileToolsHit item) {
		List<Mod> explicitMods = item.getExplicitMods();
		IntStream.range(0, explicitMods.size())
			.forEachOrdered(i -> {
				String m = explicitMods.get(i).toDisplay();
				Label l = explicitModsLbls.get(i);
				l.setText(m);
			});
	}
}