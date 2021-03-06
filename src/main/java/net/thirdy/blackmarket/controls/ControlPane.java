/*
 * Copyright (C) 2015 thirdy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.thirdy.blackmarket.controls;

import static com.google.common.collect.Iterables.toArray;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static javafx.collections.FXCollections.observableList;
import static org.elasticsearch.common.lang3.StringUtils.trimToEmpty;
import static org.elasticsearch.common.lang3.StringUtils.trimToNull;
import static org.elasticsearch.index.query.FilterBuilders.andFilter;
import static org.elasticsearch.index.query.FilterBuilders.notFilter;
import static org.elasticsearch.index.query.FilterBuilders.orFilter;
import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
import static org.elasticsearch.index.query.FilterBuilders.termFilter;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import io.jexiletools.es.model.Currencies;
import io.jexiletools.es.model.League;
import io.jexiletools.es.model.Rarity;
import io.jexiletools.es.modsmapping.ModsMapping.ModMapping;
import io.jexiletools.es.modsmapping.ModsMapping.Type;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.thirdy.blackmarket.Main;
import net.thirdy.blackmarket.controls.ModSelectionPane.Mod;
import net.thirdy.blackmarket.domain.DivinationCard;
import net.thirdy.blackmarket.domain.RangeOptional;
import net.thirdy.blackmarket.domain.SearchEventHandler;
import net.thirdy.blackmarket.domain.Unique;
import net.thirdy.blackmarket.fxcontrols.Clearable;
import net.thirdy.blackmarket.fxcontrols.FourColorIntegerTextField;
import net.thirdy.blackmarket.fxcontrols.IntegerTextField;
import net.thirdy.blackmarket.fxcontrols.RangeDoubleTextField;
import net.thirdy.blackmarket.fxcontrols.RangeIntegerTextField;
import net.thirdy.blackmarket.fxcontrols.SmallIcon;
import net.thirdy.blackmarket.fxcontrols.ToggleButtonToolBar;
import net.thirdy.blackmarket.fxcontrols.TriStateButton;
import net.thirdy.blackmarket.fxcontrols.TriStateButton.State;
import net.thirdy.blackmarket.fxcontrols.TwoColumnGridPane;
import net.thirdy.blackmarket.fxcontrols.autocomplete.BlackmarketTextField;

/**
 * @author thirdy
 *
 */
public class ControlPane extends BorderPane {
	
	private static final String DEFAULT_SEARCH_SIZE = "500";

	private HBox top;
	
	private ComboBox<String> cmbxLeague;

	private ItemTypePanes itemTypesPanes;

	private TextField tfName;
	private Button btnSearch;
	private ToggleButton btnDurianMode = new ToggleButton("Durian Notifier");
	
	private Label lblHitCount = new Label();
	private Label lblLadderServiceStatus = new Label();
	private ProgressIndicator progIndctrLadderService = new ProgressIndicator(-1.0f);
	private Button btnAbout = new Button("About");
	private ToggleButton toggleAdvanceMode = new ToggleButton("Advance Mode");
	
	private TextArea txtAreaJson = new TextArea();

	private GridPane simpleSearchGridPane;
	private Button btnReset;

	private RangeDoubleTextField tfDPS = new RangeDoubleTextField();
	private RangeDoubleTextField tfeDPS = new RangeDoubleTextField();
	private RangeDoubleTextField tfpDPS = new RangeDoubleTextField();
	private RangeDoubleTextField tfAPS = new RangeDoubleTextField();
	private RangeDoubleTextField tfCritChance = new RangeDoubleTextField();
	private TriStateButton btn3Corrupt = new TriStateButton(State.Or);
	private TriStateButton btn3Identified = new TriStateButton(State.Or);
	private TriStateButton btn3Crafted = new TriStateButton(State.Or);
	private RangeIntegerTextField tfAttrStr = new RangeIntegerTextField();
	private RangeIntegerTextField tfAttrDex = new RangeIntegerTextField();
	private RangeIntegerTextField tfAttrInt = new RangeIntegerTextField();
	private RangeIntegerTextField tfAttrTotal = new RangeIntegerTextField();
	private IntegerTextField tfSize = new IntegerTextField("");
	
	private RangeIntegerTextField tfLife = new RangeIntegerTextField();
	private RangeIntegerTextField tfColdRes = new RangeIntegerTextField();
	private RangeIntegerTextField tfFireRes = new RangeIntegerTextField();
	private RangeIntegerTextField tfLightningRes = new RangeIntegerTextField();
	private RangeIntegerTextField tfChaosRes = new RangeIntegerTextField();
	private RangeIntegerTextField tfTotalEleRes = new RangeIntegerTextField();
	private RangeIntegerTextField tfArmour = new RangeIntegerTextField();
	private RangeIntegerTextField tfEvasion = new RangeIntegerTextField();
	private RangeIntegerTextField tfEnergyShield = new RangeIntegerTextField();
	private RangeIntegerTextField tfBlock = new RangeIntegerTextField();
//	private ComboBox<Rarity> cmbxRarity = new ComboBox<>(FXCollections.observableArrayList(Rarity.values()));
	private ToggleButtonToolBar<Rarity> toggleTbRarity = new ToggleButtonToolBar<Rarity>(true, asList(Rarity.values()));
	
	private RangeIntegerTextField tfLvlReq = new RangeIntegerTextField();
	private RangeIntegerTextField tfStrReq = new RangeIntegerTextField();
	private RangeIntegerTextField tfDexReq = new RangeIntegerTextField();
	private RangeIntegerTextField tfIntReq = new RangeIntegerTextField();
	
	private RangeDoubleTextField tfQuality = new RangeDoubleTextField();
	
	private RangeIntegerTextField tfSockets = new RangeIntegerTextField();
	private RangeIntegerTextField tfLink = new RangeIntegerTextField();
	private FourColorIntegerTextField tfSockColors = new FourColorIntegerTextField();
	private FourColorIntegerTextField tfLinks = new FourColorIntegerTextField();
	
	private ToggleButton btnSortByShopUpdate = new ToggleButton("Sort by Last Shop Updated");
	private ToggleButton btnVerified = new ToggleButton("Verified");
	private ToggleButton btnOnlineOnly = new ToggleButton("Ladder Online Only");
	
	private PriceControl priceControl = new PriceControl();

//	private ModsSelectionPane modsSelectionPane;
	private ModSelectionPane modSelectionPane;

	private ScrollPane simpleSearchScrollPane;
	
	public ControlPane(SearchEventHandler searchEventHandler) {
		setId("controlPane");
		btnAbout.setOnAction(e -> Dialogs.showAbout());
		tfSize.setText(DEFAULT_SEARCH_SIZE);
		toggleAdvanceMode.setOnAction(e -> {
			if(toggleAdvanceMode.isSelected()) {
				txtAreaJson.setText(buildSimpleSearch());
				setCenter(txtAreaJson);
			}
			else setCenter(simpleSearchScrollPane);
		});
		lblLadderServiceStatus.setTooltip(new Tooltip());
		progIndctrLadderService.setMaxSize(15, 15);
		
		top = new HBox(5);
		top.getChildren().addAll(lblHitCount, newSpacer(), progIndctrLadderService, lblLadderServiceStatus);
		setTop(top);
		
	    List<String> namesList = new ArrayList<>();
	    namesList.addAll(Arrays.asList(Unique.names));
	    namesList.addAll(Arrays.asList(DivinationCard.names));
	    namesList.addAll(Currencies.validDisplayNames());
//		tfName = new AutoCompleteTextField<String>(namesList, 300);
		tfName = new BlackmarketTextField<String>(namesList);

	    tfName.setPrefWidth(220);
		
	    cmbxLeague = new ComboBox<>(observableList(League.names()));
	    cmbxLeague.setEditable(false);
	    cmbxLeague.getSelectionModel().selectFirst();
	    cmbxLeague.setMinWidth(220);
	    
//	    modsSelectionPane = new ModsSelectionPane();
//	    itemTypesPanes = new ItemTypePanes(modsSelectionPane);
	    modSelectionPane = new ModSelectionPane();
	    itemTypesPanes = new ItemTypePanes(modSelectionPane);
	    
	    simpleSearchGridPane = new GridPane();
	    simpleSearchGridPane.setGridLinesVisible(Main.DEVELOPMENT_MODE);
	    simpleSearchGridPane.setPadding(new Insets(0));
	    simpleSearchGridPane.setHgap(5);
	    ColumnConstraints column1 = new ColumnConstraints();
	    column1.setHgrow(Priority.ALWAYS);
	    column1.setPercentWidth(28);
	    ColumnConstraints column2 = new ColumnConstraints();
	    column2.setHgrow(Priority.ALWAYS);
	    column2.setPercentWidth(24);
	    ColumnConstraints column3 = new ColumnConstraints();
	    column3.setHgrow(Priority.ALWAYS);
	    column3.setPercentWidth(24);
	    ColumnConstraints column4 = new ColumnConstraints();
	    column4.setHgrow(Priority.ALWAYS);
	    column4.setPercentWidth(24);
	    
	    simpleSearchGridPane.getColumnConstraints().addAll(column1, column2, column3, column4);

	    // Column 1
	    simpleSearchGridPane.add(new TwoColumnGridPane(56.0,
	    		"League:", cmbxLeague,
	    		"Name:"  , tfName,
	    		"Armour:"  , itemTypesPanes.getItemTypePane1(),
	    		"Weapon:"  , itemTypesPanes.getItemTypePane3(),
	    		"Misc:"  , itemTypesPanes.getItemTypePane2()), 0, 0);
	    
	    // Column 2
		TwoColumnGridPane col2Pane = new TwoColumnGridPane(
	    		"DPS:"	 , tfDPS,
	    		"pDPS:"  , tfpDPS,
	    		"eDPS:"  , tfeDPS,
	    		"APS:"  ,  tfAPS,
	    		"Crit Chance:"  , tfCritChance,
	    		new Label("Corrupted ", new SmallIcon(Currencies.vaal)) , btn3Corrupt,
	    		new Label("Identified ", new SmallIcon(Currencies.id)) , btn3Identified,
	    		new Label("Crafted ", new SmallIcon(Currencies.fuse)) , btn3Crafted,
	    		"Strength:"	, tfAttrStr,
	    		"Dexterity:"	, tfAttrDex,
	    		"Intelligence:"	, tfAttrInt,
	    		"Attributes:"	, tfAttrTotal,
	    		"Buyout:", priceControl
	    		);
		simpleSearchGridPane.add(col2Pane, 1, 0);

	    // Column 3
		simpleSearchGridPane.add(new TwoColumnGridPane(
				"Life:"	, tfLife,
				"Cold Res:"	, tfColdRes,
				"Fire Res:"	, tfFireRes,
				"Lightning Res:", tfLightningRes,
				"Chaos Res:", tfChaosRes,
				"Elemental Res:", tfTotalEleRes,
	    		"Armour:"	, tfArmour,
	    		"Evasion:"   , tfEvasion,
	    		"Energy Shield:"   , tfEnergyShield,
	    		"Block:"  , tfBlock,
	    		"# Sockets:" , tfSockets,
	    		"# Links:" , tfLink,
	    		"Size: ", tfSize
	    		), 2, 0);
		
		// Column 4
		tfLinks.setDisable(true); // TODO
		simpleSearchGridPane.add(new TwoColumnGridPane(
				"Required Lvl:"	, tfLvlReq,
				"Required Str:"   , tfStrReq,
				"Required Dex:"   , tfDexReq,
				"Required Int:"  , tfIntReq,
				"Quality %:"	 , tfQuality,
				"Socket Colors:" , tfSockColors,
	    		"Link Setup:" , tfLinks,
				"Rarity:", toggleTbRarity,
	    		"Verified:",  btnVerified
				), 3, 0);
		
		btnVerified.setSelected(true);
		
		btnSearch = new Button("Search");
		btnSearch.setOnAction(e -> {
			String json = toggleAdvanceMode.isSelected() ?
					buildAdvanceSearch()
					: buildSimpleSearch();
			searchEventHandler.search(json);
		});
		btnSearch.setPrefWidth(500);
		
		btnReset = new Button("Reset");
		btnReset.setOnAction(e -> resetForm());
		
		HBox bottomPane = new HBox(toggleAdvanceMode, btnDurianMode, newSpacer(), btnSearch, newSpacer(), btnOnlineOnly, btnAbout, btnReset);
		
		GridPane.setHalignment(bottomPane, HPos.CENTER);
		simpleSearchGridPane.setAlignment(Pos.CENTER);
		simpleSearchGridPane.setMaxSize(1060, 260);
		
	    
		VBox contentVBox = new VBox(10.0, simpleSearchGridPane, modSelectionPane);
		contentVBox.setMaxWidth(Double.MAX_VALUE);
		contentVBox.setAlignment(Pos.CENTER);
		simpleSearchScrollPane = new ScrollPane(new StackPane(contentVBox));
		simpleSearchScrollPane.setFitToWidth(true);
		simpleSearchScrollPane.getStyleClass().add("edge-to-edge");
		
		setCenter(simpleSearchScrollPane);
		setBottom(bottomPane);
	}

	public ToggleButton getBtnDurianMode() {
		return btnDurianMode;
	}
	
	public ToggleButton getBtnOnlineOnly() {
		return btnOnlineOnly;
	}
	
	public Button getBtnSearch() {
		return btnSearch;
	}
	
	public Label getLblLadderServiceStatus() {
		return lblLadderServiceStatus;
	}
	
	public ProgressIndicator getProgIndctrLadderService() {
		return progIndctrLadderService;
	}
	
	public Label getSearchHitLabel() {
		return lblHitCount;
	}

	private Region newSpacer() {
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		return spacer;
	}
	
	private String buildAdvanceSearch() {
		return trimToEmpty(txtAreaJson.getText());
	}
	
	private String buildSimpleSearch() {
		List<FilterBuilder> filters = new LinkedList<>();
		String json = null;
		
		// Col 1
//		ofNullable(tfName.getSelectionModel().getSelectedItem()).map(s -> filters.add(termFilter("info.name", s)));
		ofNullable(tfName.getText()).map(s -> trimToNull(s)).map(s ->
					filters.add(orFilter(
							termFilter("info.name", s),
							termFilter("info.fullName", s),
							termFilter("info.typeLine", s)
							))
				);
		filters.add(termFilter("attributes.league", cmbxLeague.getSelectionModel().getSelectedItem()));
		itemTypesFilter().ifPresent(t -> filters.add(t));
		
		// Col 2
		tfDPS.val().ifPresent(t -> filters.add(t.rangeFilter("properties.Weapon.Total DPS")));
		tfpDPS.val().ifPresent(t -> filters.add(t.rangeFilter("properties.Weapon.Physical DPS")));
		tfeDPS.val().ifPresent(t -> filters.add(t.rangeFilter("properties.Weapon.Elemental DPS")));
		tfAPS.val().ifPresent(t -> filters.add(t.rangeFilter("properties.Weapon.Attacks per Second")));
		tfCritChance.val().ifPresent(t -> filters.add(t.rangeFilter("properties.Weapon.Critical Strike Chance")));
		if(btn3Corrupt.stateProperty().get() != State.Or) 		filters.add(termFilter("attributes.corrupted", btn3Corrupt.stateProperty().get() == State.And)); 
		if(btn3Identified.stateProperty().get() != State.Or) 	filters.add(termFilter("attributes.identified", btn3Identified.stateProperty().get() == State.And));
		if(btn3Crafted.stateProperty().get() != State.Or) 		filters.add(
				btn3Crafted.stateProperty().get() == State.And ? rangeFilter("attributes.craftedModCount").gt(0)
						: rangeFilter("attributes.craftedModCount").lte(0));
		tfAttrStr.val().ifPresent(t -> filters.add(t.rangeFilter("modsPseudo.flatSumStr")));
		tfAttrDex.val().ifPresent(t -> filters.add(t.rangeFilter("modsPseudo.flatSumDex")));
		tfAttrInt.val().ifPresent(t -> filters.add(t.rangeFilter("modsPseudo.flatSumInt")));
		tfAttrTotal.val().ifPresent(t -> filters.add(t.rangeFilter("modsPseudo.flatAttributesTotal")));
		
		// Col 3
		tfLife.val().ifPresent(t -> filters.add(t.rangeFilter("modsPseudo.maxLife")));
		tfColdRes.val().ifPresent(t -> filters.add(t.rangeFilter("modsPseudo.eleResistSumCold")));
		tfFireRes.val().ifPresent(t -> filters.add(t.rangeFilter("modsPseudo.eleResistSumFire")));
		tfLightningRes.val().ifPresent(t -> filters.add(t.rangeFilter("modsPseudo.eleResistSumLightning")));
		tfChaosRes.val().ifPresent(t -> filters.add(t.rangeFilter("modsPseudo.eleResistSumChaos")));
		tfTotalEleRes.val().ifPresent(t -> filters.add(t.rangeFilter("modsPseudo.eleResistTotal")));
		tfArmour.val().ifPresent(t -> filters.add(t.rangeFilter("properties.Armour.Armour")));
		tfEvasion.val().ifPresent(t -> filters.add(t.rangeFilter("properties.Armour.Evasion Rating")));
		tfEnergyShield.val().ifPresent(t -> filters.add(t.rangeFilter("properties.Armour.Energy Shield")));
		tfBlock.val().ifPresent(t -> filters.add(t.rangeFilter("properties.Armour.Chance to Block")));
		tfSockets.val().ifPresent(t -> filters.add(t.rangeFilter("sockets.socketCount")));
		tfLink.val().ifPresent(t -> filters.add(t.rangeFilter("sockets.largestLinkGroup")));
		
		toggleTbRarity.val().ifPresent(list -> filters.add(rarityOrFilter(list)));
		
		// Col 4
		tfLvlReq.val().ifPresent(t -> filters.add(t.rangeFilter("requirements.Level")));
		tfStrReq.val().ifPresent(t -> filters.add(t.rangeFilter("requirements.Str")));
		tfDexReq.val().ifPresent(t -> filters.add(t.rangeFilter("requirements.Dex")));
		tfIntReq.val().ifPresent(t -> filters.add(t.rangeFilter("requirements.Int")));
		tfQuality.val().ifPresent(t -> qualityFilter(t).ifPresent(f -> filters.add(f)));
		tfSockColors.val().ifPresent(s -> filters.add(termFilter("sockets.allSocketsSorted", s)));
		
		// Col 5
		if (priceControl.isBuyoutOnly()) {
			if (priceControl.anyPrice()) {
				filters.add(notFilter(termFilter("shop.currency", "NONE")));
			} else {
				priceControl.val().ifPresent(price -> filters.add(price.rangeFilter("shop.chaosEquiv")));
			}
		}
		
		// Mods
//		modsSelectionPane.implicit().ifPresent(mod -> filters.add(implicitModFilter(mod)));
//		modsSelectionPane.explicitMods().ifPresent(mod -> filters.add(explicitModFilter(mod)));
		
		if(btnVerified.isSelected())
			filters.add(termFilter("shop.verified", "YES"));
		
		// Final Build
		FilterBuilder filter = andFilter(toArray(filters, FilterBuilder.class));
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		
		QueryBuilder query = null;
		if (modSelectionPane.mods().isPresent()) {
			query = modFilter(modSelectionPane.mods().get());
		}
		
		searchSourceBuilder.query(filteredQuery(query, filter));
		searchSourceBuilder.size(tfSize.getOptionalValue().orElse(500));
		
		searchSourceBuilder.sort("shop.chaosEquiv", SortOrder.ASC);
		if(btnSortByShopUpdate.isSelected())
			searchSourceBuilder.sort("shop.updated", SortOrder.DESC);
//		searchSourceBuilder. sort(SortBuilders.
//				fieldSort("shop.chaosEquiv").order(SortOrder.ASC));
//		btnSortByShopUpdate.isSelected()
		
		
//        JsonObject sortJson=  new JsonObject();
//        json. put("sort", sortJson);
//      
//        JsonObject sortDateJson=new JsonObject();
//        sortJson.put("age", sortDateJson);
//        sortDateJson.put("order", "asc");
		
		json = searchSourceBuilder.toString();

		return json;
	}


	private FilterBuilder rarityOrFilter(List<Rarity> list) {
		List<TermFilterBuilder> filters = list.stream()
			.map(l -> termFilter("attributes.rarity", l.displayName()))
			.collect(Collectors.toList());
		TermFilterBuilder[] array = new TermFilterBuilder[filters.size()];
		array = filters.toArray(array); 
		return orFilter(array);
	}
	
	private QueryBuilder modFilter(List<Mod> mods) {
		BoolQueryBuilder modQuery = boolQuery(); 
		
		mods.stream()
		.forEach(mod -> {
			QueryBuilder queryBuilder = null;
			ModMapping selectedMod = mod.modMapping;  
			Optional<RangeOptional> lowerRange = mod.lowerRange.get();
			Optional<RangeOptional> higherRange = mod.higherRange.get();
			
			if (selectedMod.getType() == Type.DOUBLE_MIN_MAX) {
				BoolQueryBuilder minMaxQueryBuilder = boolQuery();
				
				minMaxQueryBuilder.must(
						lowerRange.orElse(RangeOptional.MIN_ZERO).rangeQuery(selectedMod.getKey())
				);

				if (higherRange.isPresent()) {
					String modifierKey = StringUtils.removeEnd(selectedMod.getKey(), ".min") + ".max";
					minMaxQueryBuilder.must(
							higherRange.get().rangeQuery(modifierKey)
					);
				}
				
				queryBuilder = minMaxQueryBuilder;
			}
			
			if (selectedMod.getType() == Type.DOUBLE) {
				queryBuilder = lowerRange.orElse(RangeOptional.MIN_ZERO).rangeQuery(selectedMod.getKey());
			}
			
			if (selectedMod.getType() == Type.BOOLEAN) {
				queryBuilder =  termQuery(selectedMod.getKey(), "true");
			}
			
			switch (mod.logic.get()) {
			case And:
				modQuery.must(queryBuilder);
				break;
			case Or:
				modQuery.should(queryBuilder);
				break;
			case Not:
				modQuery.mustNot(queryBuilder);
				break;
			}
		});
		modSelectionPane.mininumShouldMatch().ifPresent(i -> modQuery.minimumNumberShouldMatch(i.intValue()));
		
		return modQuery;
	}

//	private FilterBuilder explicitModFilter(List<Mod> mod) {
//		BoolFilterBuilder exFilter = boolFilter();
//		mod.stream()
//			.forEach(m -> {
//				FilterBuilder fb = null;
//				ModMapping selectedMod = m.tfMod.item();  
//				if (m.rangeDoubleTf.val().isPresent()) {
//					fb = m.rangeDoubleTf.val().get().rangeFilter(selectedMod.getKey());
//				} else {
//					fb = existsFilter(selectedMod.getKey());
//				}
//				switch (m.logic.state()) {
//				case checked:
//					exFilter.must(fb);
//					break;
//				case unchecked:
//					exFilter.should(fb);
//					break;
//				case undefined:
//					exFilter.mustNot(fb);
//					break;
//				}
//			});
//		return exFilter;
//	}
//
//	private FilterBuilder implicitModFilter(Mod mod) {
//		BoolFilterBuilder impFil = boolFilter();
//		FilterBuilder fb = null;
//		ModMapping selectedMod = mod.tfMod.item(); 
//		if (mod.rangeDoubleTf.val().isPresent()) {
//			fb = mod.rangeDoubleTf.val().get().rangeFilter(selectedMod.getKey());
//		} else {
//			fb = existsFilter(selectedMod.getKey());
//		}
//		switch (mod.logic.state()) {
//		case checked:
//			impFil.must(fb);
//			break;
//		case unchecked:
//			impFil.should(fb);
//			break;
//		case undefined:
//			impFil.mustNot(fb);
//			break;
//		}
//		return impFil;
//	}


	private Optional<FilterBuilder> qualityFilter(RangeOptional t) {
		if(!itemTypesPanes.getSelected().isEmpty()) {
			List<FilterBuilder> qualityFilters = itemTypesPanes.getSelected().stream()
				.map(it -> format("properties.%s.Quality", it.itemType()))
				.map(name -> t.rangeFilter(name))
				.collect(Collectors.toList());
			return Optional.of(orFilter(toArray(qualityFilters, FilterBuilder.class)));
		}
		return Optional.empty();
	}
	
	private Optional<OrFilterBuilder> itemTypesFilter() {
		if (!itemTypesPanes.getSelected().isEmpty()) {
			List<FilterBuilder> itemTypeFilters = itemTypesPanes.getSelected()
					.stream()
					.map(it -> {
						FilterBuilder itFilter = termFilter("attributes.itemType", it.itemType());
						if (it.equipType() != null) {
							itFilter = andFilter(itFilter, termFilter("attributes.equipType", it.equipType()));
						}
						return itFilter;
					})
					.collect(Collectors.toList());
			return Optional.of(orFilter(toArray(itemTypeFilters, FilterBuilder.class)));
		}
		return Optional.empty();
	}

	public void installCollapseButton(Button showCollapseButton) {
		top.getChildren().add(showCollapseButton);
	}
	public void fireSearchEvent() {
		btnSearch.fire();
	}
	
	
	private void resetForm() {
		itemTypesPanes.unselectAll();
		tfName.setText("");
		toggleTbRarity.unselectAll();
		tfSize.setText(DEFAULT_SEARCH_SIZE);
		btn3Corrupt.setState(State.Or);
		btn3Identified.setState(State.Or);
		btn3Crafted.setState(State.Or);

		List<Clearable> clearables = asList(
				tfDPS,
				tfeDPS,
				tfpDPS,
				tfAPS,
				tfCritChance,
				tfAttrStr,
				tfAttrDex,
				tfAttrInt,
				tfAttrTotal,
				tfLife,
				tfColdRes,
				tfFireRes,
				tfLightningRes,
				tfChaosRes,
				tfTotalEleRes,
				tfArmour,
				tfEvasion,
				tfEnergyShield,
				tfBlock,
				tfLvlReq,
				tfStrReq,
				tfDexReq,
				tfIntReq,
				tfQuality,
				tfSockets,
				tfLink,
				tfSockColors,
				tfLinks);
		clearables.stream().forEach(c -> c.clear());
	}
}
