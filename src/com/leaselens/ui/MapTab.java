package com.leaselens.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.leaselens.model.Apartment;
import com.leaselens.model.Status;
import com.leaselens.service.ApartmentService;

/**
 * This class is the Map View tab showing apartments on a real map
 * It use JavaFX Canvas to draw OpenStreetMap tile images
 * Each apartment is a colored circle on the map
 *
 * pre-condition: service should not be null
 * post-condition: map tab is created
 */
public class MapTab {

    private ApartmentService service;
    private VBox content;
    private Canvas tileCanvas;    // bottom canvas for map tiles
    private Canvas markerCanvas;  // top canvas for apartment pins
    private Label infoLabel;

    // map center coordinates (Boston default)
    private double centerLat = 42.3601;
    private double centerLon = -71.0589;

    // zoom level and tile size (OpenStreetMap standard)
    private int zoom = 13;
    private int tileSize = 256;

    /**
     * This constructor is making the map tab
     * @param service the apartment service
     *
     * pre-condition: service should not be null
     * post-condition: tab is built with map canvas
     */
    public MapTab(ApartmentService service) {
        this.service = service;
        this.content = new VBox(10);
        buildTab();
    }

    /**
     * This method is building the map tab UI with canvas
     *
     * pre-condition: none
     * post-condition: map canvas and controls is created
     */
    private void buildTab() {
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f0f2f5;");

        // header row
        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label header = new Label("Map View");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #1a237e;");
        HBox.setHgrow(header, Priority.ALWAYS);

        Button refreshButton = new Button("Refresh Map");
        refreshButton.setFont(Font.font("Arial", 13));
        refreshButton.setPadding(new Insets(8, 20, 8, 20));
        refreshButton.setStyle(
            "-fx-background-color: #1a237e; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        refreshButton.setOnAction(e -> refresh());
        headerRow.getChildren().addAll(header, refreshButton);

        // legend
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(10, 15, 10, 15));
        legend.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );
        legend.getChildren().addAll(
            makeLegendItem("New", "#2196f3"),
            makeLegendItem("Shortlisted", "#4caf50"),
            makeLegendItem("Toured", "#ff9800"),
            makeLegendItem("Rejected", "#f44336")
        );

        // info label
        infoLabel = new Label("Click 'Refresh Map' to load apartment pins");
        infoLabel.setFont(Font.font("Arial", 13));
        infoLabel.setStyle("-fx-text-fill: #666;");

        // map area - tile canvas at bottom, marker canvas on top
        StackPane mapPane = new StackPane();
        mapPane.setStyle("-fx-background-color: #b8d4e8;"); // water color while loading
        VBox.setVgrow(mapPane, Priority.ALWAYS);

        tileCanvas = new Canvas();
        markerCanvas = new Canvas();

        // make both canvases resize with the pane
        tileCanvas.widthProperty().bind(mapPane.widthProperty());
        tileCanvas.heightProperty().bind(mapPane.heightProperty());
        markerCanvas.widthProperty().bind(mapPane.widthProperty());
        markerCanvas.heightProperty().bind(mapPane.heightProperty());

        // redraw when the canvas gets resized
        tileCanvas.widthProperty().addListener(e -> drawTiles());
        tileCanvas.heightProperty().addListener(e -> drawTiles());

        // zoom in / zoom out buttons shown on top-left of map
        VBox zoomButtons = new VBox(2);
        zoomButtons.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(zoomButtons, Pos.TOP_LEFT);
        StackPane.setMargin(zoomButtons, new Insets(10));

        Button zoomInBtn = new Button("+");
        Button zoomOutBtn = new Button("-");

        String zoomStyle =
            "-fx-background-color: white; " +
            "-fx-font-size: 18px; -fx-font-weight: bold; " +
            "-fx-min-width: 36; -fx-min-height: 36; " +
            "-fx-max-width: 36; -fx-max-height: 36; " +
            "-fx-background-radius: 4; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);";

        zoomInBtn.setStyle(zoomStyle);
        zoomOutBtn.setStyle(zoomStyle);

        zoomInBtn.setOnAction(e -> {
            if (zoom < 18) { zoom++; drawTiles(); }
        });
        zoomOutBtn.setOnAction(e -> {
            if (zoom > 5) { zoom--; drawTiles(); }
        });

        zoomButtons.getChildren().addAll(zoomInBtn, zoomOutBtn);

        // scroll wheel zoom on the map
        mapPane.setOnScroll(e -> {
            if (e.getDeltaY() > 0) {
                if (zoom < 18) { zoom++; drawTiles(); }
            } else {
                if (zoom > 5) { zoom--; drawTiles(); }
            }
        });

        mapPane.getChildren().addAll(tileCanvas, markerCanvas, zoomButtons);

        content.getChildren().addAll(headerRow, legend, infoLabel, mapPane);

        // draw the map when first loading
        drawTiles();
    }

    /**
     * This method is making a legend item with a color dot
     * @param text the label text
     * @param color the dot color
     * @return HBox legend item
     *
     * pre-condition: none
     * post-condition: legend item is returned
     */
    private HBox makeLegendItem(String text, String color) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);

        Label dot = new Label("  ");
        dot.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-background-radius: 10; " +
            "-fx-min-width: 14; -fx-min-height: 14; " +
            "-fx-max-width: 14; -fx-max-height: 14;"
        );

        Label label = new Label(text);
        label.setFont(Font.font("Arial", 13));

        item.getChildren().addAll(dot, label);
        return item;
    }

    /**
     * This method is drawing all the OpenStreetMap tiles on the tile canvas
     * It calculate which tiles are needed and load them as JavaFX images
     *
     * pre-condition: none
     * post-condition: map tiles is drawn on canvas
     */
    private void drawTiles() {
        double width = tileCanvas.getWidth();
        double height = tileCanvas.getHeight();
        if (width <= 0 || height <= 0) return;

        GraphicsContext tgc = tileCanvas.getGraphicsContext2D();
        tgc.clearRect(0, 0, width, height);

        // fill background with water color while tiles load
        tgc.setFill(Color.web("#b8d4e8"));
        tgc.fillRect(0, 0, width, height);

        // figure out which tile the center is on
        double centerTileX = lonToTileDouble(centerLon);
        double centerTileY = latToTileDouble(centerLat);

        // top-left tile index
        int startTileX = (int) Math.floor(centerTileX - width / 2.0 / tileSize);
        int startTileY = (int) Math.floor(centerTileY - height / 2.0 / tileSize);

        // pixel position of the top-left tile on screen
        double startPixelX = (startTileX - centerTileX + width / 2.0 / tileSize) * tileSize;
        double startPixelY = (startTileY - centerTileY + height / 2.0 / tileSize) * tileSize;

        // how many tiles we need to cover the canvas
        int tilesAcross = (int) Math.ceil(width / tileSize) + 2;
        int tilesDown = (int) Math.ceil(height / tileSize) + 2;

        int maxTile = (int) Math.pow(2, zoom);

        for (int tx = 0; tx < tilesAcross; tx++) {
            for (int ty = 0; ty < tilesDown; ty++) {
                // wrap tile X around (the world repeats horizontally)
                int tileX = ((startTileX + tx) % maxTile + maxTile) % maxTile;
                int tileY = startTileY + ty;

                // skip tiles outside valid range
                if (tileY < 0 || tileY >= maxTile) continue;

                double pixelX = startPixelX + tx * tileSize;
                double pixelY = startPixelY + ty * tileSize;

                // load tile image from OpenStreetMap
                String url = "https://tile.openstreetmap.org/" + zoom + "/" + tileX + "/" + tileY + ".png";
                Image tile = new Image(url, tileSize, tileSize, false, false, true);

                final double px = pixelX;
                final double py = pixelY;

                // when tile finishes loading, draw it
                tile.progressProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal.doubleValue() >= 1.0 && !tile.isError()) {
                        Platform.runLater(() -> {
                            tgc.drawImage(tile, px, py, tileSize, tileSize);
                            drawMarkers(); // keep markers on top after each tile draws
                        });
                    }
                });

                // draw immediately if tile is already in memory
                if (tile.getProgress() >= 1.0 && !tile.isError()) {
                    tgc.drawImage(tile, pixelX, pixelY, tileSize, tileSize);
                }
            }
        }

        drawMarkers();
    }

    /**
     * This method is drawing colored circle markers for each apartment
     * It use the marker canvas which is on top of the tile canvas
     *
     * pre-condition: none
     * post-condition: markers is drawn for all apartments that have coordinates
     */
    private void drawMarkers() {
        double width = markerCanvas.getWidth();
        double height = markerCanvas.getHeight();
        if (width <= 0 || height <= 0) return;

        GraphicsContext mgc = markerCanvas.getGraphicsContext2D();
        mgc.clearRect(0, 0, width, height);

        int count = 0;
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            double lat = apt.getLatitude();
            double lon = apt.getLongitude();

            // skip apartments with no location data
            if (lat == 0 && lon == 0) continue;

            double x = lonToPixelX(lon, width);
            double y = latToPixelY(lat, height);

            Color color = getMarkerColor(apt.getStatus());

            // draw filled circle
            mgc.setFill(color);
            mgc.fillOval(x - 9, y - 9, 18, 18);

            // draw white border around circle
            mgc.setStroke(Color.WHITE);
            mgc.setLineWidth(2.5);
            mgc.strokeOval(x - 9, y - 9, 18, 18);

            // draw apartment name next to the marker
            mgc.setFill(Color.web("#111111"));
            mgc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            mgc.fillText(apt.getName(), x + 12, y + 4);

            count++;
        }

        // update status label
        final int finalCount = count;
        infoLabel.setText(finalCount + " apartment(s) shown on map. Apartments without coordinates are not shown.");
    }

    /**
     * This method converts longitude to tile X coordinate (as decimal)
     * @param lon the longitude value
     * @return tile X position as decimal
     *
     * pre-condition: none
     * post-condition: tile X is returned
     */
    private double lonToTileDouble(double lon) {
        return (lon + 180.0) / 360.0 * Math.pow(2, zoom);
    }

    /**
     * This method converts latitude to tile Y coordinate (as decimal)
     * @param lat the latitude value
     * @return tile Y position as decimal
     *
     * pre-condition: none
     * post-condition: tile Y is returned
     */
    private double latToTileDouble(double lat) {
        double latRad = Math.toRadians(lat);
        return (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * Math.pow(2, zoom);
    }

    /**
     * This method converts longitude to pixel X position on canvas
     * @param lon the longitude
     * @param canvasWidth the canvas width
     * @return pixel X position
     *
     * pre-condition: none
     * post-condition: pixel X is returned
     */
    private double lonToPixelX(double lon, double canvasWidth) {
        return canvasWidth / 2.0 + (lonToTileDouble(lon) - lonToTileDouble(centerLon)) * tileSize;
    }

    /**
     * This method converts latitude to pixel Y position on canvas
     * @param lat the latitude
     * @param canvasHeight the canvas height
     * @return pixel Y position
     *
     * pre-condition: none
     * post-condition: pixel Y is returned
     */
    private double latToPixelY(double lat, double canvasHeight) {
        return canvasHeight / 2.0 + (latToTileDouble(lat) - latToTileDouble(centerLat)) * tileSize;
    }

    /**
     * This method is giving the color for a marker based on apartment status
     * @param status the apartment status
     * @return Color for the marker
     *
     * pre-condition: status should not be null
     * post-condition: color is returned
     */
    private Color getMarkerColor(Status status) {
        if (status == Status.NEW) return Color.web("#2196f3");
        if (status == Status.SHORTLISTED) return Color.web("#4caf50");
        if (status == Status.TOURED) return Color.web("#ff9800");
        return Color.web("#f44336");
    }

    /**
     * This method is refreshing the map with latest apartment data
     * It also re-center the map on the apartments if any have coordinates
     *
     * pre-condition: none
     * post-condition: map is redrawn with current apartment pins
     */
    /**
     * This method is centering the map on a specific apartment location
     * It is called when user click "View on Map" from the apartments tab
     * @param lat the latitude to center on
     * @param lon the longitude to center on
     *
     * pre-condition: lat and lon should be valid coordinates
     * post-condition: map is redrawn centered on the given location
     */
    public void centerOn(double lat, double lon) {
        centerLat = lat;
        centerLon = lon;
        zoom = 15;
        drawTiles();
    }

    public void refresh() {
        // center on average position of all apartments with coordinates
        int count = 0;
        double sumLat = 0;
        double sumLon = 0;
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            if (apt.getLatitude() != 0 || apt.getLongitude() != 0) {
                sumLat = sumLat + apt.getLatitude();
                sumLon = sumLon + apt.getLongitude();
                count++;
            }
        }
        if (count > 0) {
            centerLat = sumLat / count;
            centerLon = sumLon / count;
        }

        drawTiles();
    }

    public VBox getContent() {
        return content;
    }
}
