package com.leaselens.ui;

import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import com.leaselens.model.Apartment;
import com.leaselens.model.Status;
import com.leaselens.app.ApartmentManager;

/**
 * This is the Map View tab showing apartments on a real map
 * It draw OpenStreetMap tiles on a Canvas
 *
 * pre-condition: service not null
 * post-condition: map tab is created
 */
public class MapTab {

    private ApartmentManager service;
    private VBox content;
    private Canvas tileCanvas;
    private Canvas markerCanvas;
    private Label infoLabel;
    private double centerLat = 42.3601;
    private double centerLon = -71.0589;
    private int zoom = 13;
    private int tileSize = 256;

    /**
     * This make new MapTab with the service
     * It set up everything for the map
     * @param service the apartment service to use
     *
     * pre-condition: service is not null
     * post-condition: map tab is ready to show
     */
    public MapTab(ApartmentManager service) {
        this.service = service;
        this.content = new VBox(10);
        buildTab();
    }

    /**
     * This build all the stuff inside the tab
     * It make header and legend and map canvas and zoom button
     *
     * pre-condition: content is not null
     * post-condition: all UI component is added to content
     */
    private void buildTab() {
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f0f2f5;");

        // header
        Label header = new Label("Map View");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #1a237e;");
        HBox.setHgrow(header, Priority.ALWAYS);

        Button refreshBtn = new Button("Refresh Map");
        refreshBtn.setFont(Font.font("Arial", 13));
        refreshBtn.setPadding(new Insets(8, 20, 8, 20));
        refreshBtn.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        refreshBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                refresh();
            }
        });

        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.getChildren().addAll(header, refreshBtn);

        // legend
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(10, 15, 10, 15));
        legend.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);");
        legend.getChildren().addAll(
            makeLegendItem("New", "#2196f3"),
            makeLegendItem("Shortlisted", "#4caf50"),
            makeLegendItem("Toured", "#ff9800"),
            makeLegendItem("Rejected", "#f44336")
        );

        infoLabel = new Label("Click 'Refresh Map' to load apartment pins");
        infoLabel.setFont(Font.font("Arial", 13));
        infoLabel.setStyle("-fx-text-fill: #666;");

        // map canvases
        StackPane mapPane = new StackPane();
        mapPane.setStyle("-fx-background-color: #b8d4e8;");
        VBox.setVgrow(mapPane, Priority.ALWAYS);

        tileCanvas = new Canvas();
        markerCanvas = new Canvas();
        tileCanvas.widthProperty().bind(mapPane.widthProperty());
        tileCanvas.heightProperty().bind(mapPane.heightProperty());
        markerCanvas.widthProperty().bind(mapPane.widthProperty());
        markerCanvas.heightProperty().bind(mapPane.heightProperty());
        tileCanvas.widthProperty().addListener(new javafx.beans.InvalidationListener() {
            public void invalidated(javafx.beans.Observable obs) {
                drawTiles();
            }
        });
        tileCanvas.heightProperty().addListener(new javafx.beans.InvalidationListener() {
            public void invalidated(javafx.beans.Observable obs) {
                drawTiles();
            }
        });

        // zoom buttons
        String zoomStyle = "-fx-background-color: white; -fx-font-size: 18px; -fx-font-weight: bold; "
            + "-fx-min-width: 36; -fx-min-height: 36; -fx-max-width: 36; -fx-max-height: 36; "
            + "-fx-background-radius: 4; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 1);";
        Button zoomIn = new Button("+");
        Button zoomOut = new Button("-");
        zoomIn.setStyle(zoomStyle);
        zoomOut.setStyle(zoomStyle);
        zoomIn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                if (zoom < 18) {
                    zoom++;
                    drawTiles();
                }
            }
        });
        zoomOut.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                if (zoom > 5) {
                    zoom--;
                    drawTiles();
                }
            }
        });

        VBox zoomBox = new VBox(2);
        zoomBox.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(zoomBox, Pos.TOP_LEFT);
        StackPane.setMargin(zoomBox, new Insets(10));
        zoomBox.getChildren().addAll(zoomIn, zoomOut);

        mapPane.setOnScroll(new javafx.event.EventHandler<javafx.scene.input.ScrollEvent>() {
            public void handle(javafx.scene.input.ScrollEvent e) {
                if (e.getDeltaY() > 0 && zoom < 18) { zoom++; drawTiles(); }
                else if (e.getDeltaY() < 0 && zoom > 5) { zoom--; drawTiles(); }
            }
        });

        mapPane.getChildren().addAll(tileCanvas, markerCanvas, zoomBox);
        content.getChildren().addAll(headerRow, legend, infoLabel, mapPane);
        drawTiles();
    }

    /**
     * This make one item for the legend with color dot and text
     * @param text the label text to show
     * @param color the hex color string for the dot
     * @return HBox with the dot and label inside
     *
     * pre-condition: text and color is not null
     * post-condition: return a HBox with colored dot and label
     */
    private HBox makeLegendItem(String text, String color) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);
        Label dot = new Label("  ");
        dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10; "
            + "-fx-min-width: 14; -fx-min-height: 14; -fx-max-width: 14; -fx-max-height: 14;");
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 13));
        item.getChildren().addAll(dot, label);
        return item;
    }

    /**
     * This draw all the OpenStreetMap tiles on canvas
     *
     * pre-condition: none
     * post-condition: map tiles is drawn
     */
    private void drawTiles() {
        double w = tileCanvas.getWidth();
        double h = tileCanvas.getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext tgc = tileCanvas.getGraphicsContext2D();
        tgc.clearRect(0, 0, w, h);
        tgc.setFill(Color.web("#b8d4e8"));
        tgc.fillRect(0, 0, w, h);

        double cTileX = lonToTile(centerLon);
        double cTileY = latToTile(centerLat);
        int startX = (int) Math.floor(cTileX - w / 2.0 / tileSize);
        int startY = (int) Math.floor(cTileY - h / 2.0 / tileSize);
        double startPxX = (startX - cTileX + w / 2.0 / tileSize) * tileSize;
        double startPxY = (startY - cTileY + h / 2.0 / tileSize) * tileSize;
        int across = (int) Math.ceil(w / tileSize) + 2;
        int down = (int) Math.ceil(h / tileSize) + 2;
        int maxTile = (int) Math.pow(2, zoom);

        for (int tx = 0; tx < across; tx++) {
            for (int ty = 0; ty < down; ty++) {
                int tileX = ((startX + tx) % maxTile + maxTile) % maxTile;
                int tileY = startY + ty;
                if (tileY < 0 || tileY >= maxTile) continue;

                double px = startPxX + tx * tileSize;
                double py = startPxY + ty * tileSize;
                String url = "https://tile.openstreetmap.org/" + zoom + "/" + tileX + "/" + tileY + ".png";
                Image tile = new Image(url, tileSize, tileSize, false, false, true);

                final double fpx = px;
                final double fpy = py;
                tile.progressProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
                    public void changed(javafx.beans.value.ObservableValue<? extends Number> obs, Number ov, Number nv) {
                        if (nv.doubleValue() >= 1.0 && !tile.isError()) {
                            Platform.runLater(new Runnable() {
                                public void run() {
                                    tgc.drawImage(tile, fpx, fpy, tileSize, tileSize);
                                    drawMarkers();
                                }
                            });
                        }
                    }
                });
                if (tile.getProgress() >= 1.0 && !tile.isError()) {
                    tgc.drawImage(tile, px, py, tileSize, tileSize);
                }
            }
        }
        drawMarkers();
    }

    /**
     * This draw all apartment marker pins on the marker canvas
     * It loop through all apartment and put colored circle on map
     *
     * pre-condition: markerCanvas is not null
     * post-condition: all apartment with coordinates is drawn on map
     */
    private void drawMarkers() {
        double w = markerCanvas.getWidth();
        double h = markerCanvas.getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext mgc = markerCanvas.getGraphicsContext2D();
        mgc.clearRect(0, 0, w, h);

        int count = 0;
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            if (apt.getLatitude() == 0 && apt.getLongitude() == 0) continue;

            double x = w / 2.0 + (lonToTile(apt.getLongitude()) - lonToTile(centerLon)) * tileSize;
            double y = h / 2.0 + (latToTile(apt.getLatitude()) - latToTile(centerLat)) * tileSize;
            Color color = getMarkerColor(apt.getStatus());

            mgc.setFill(color);
            mgc.fillOval(x - 9, y - 9, 18, 18);
            mgc.setStroke(Color.WHITE);
            mgc.setLineWidth(2.5);
            mgc.strokeOval(x - 9, y - 9, 18, 18);
            mgc.setFill(Color.web("#111111"));
            mgc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            mgc.fillText(apt.getName(), x + 12, y + 4);
            count++;
        }
        infoLabel.setText(count + " apartment(s) shown on map. Apartments without coordinates are not shown.");
    }

    /**
     * This convert longitude to tile x coordinate
     * @param lon the longitude value
     * @return the tile x position as double
     *
     * pre-condition: lon is valid longitude number
     * post-condition: return correct tile x for the zoom level
     */
    private double lonToTile(double lon) {
        return (lon + 180.0) / 360.0 * Math.pow(2, zoom);
    }

    /**
     * This convert latitude to tile y coordinate
     * @param lat the latitude value
     * @return the tile y position as double
     *
     * pre-condition: lat is valid latitude number
     * post-condition: return correct tile y for the zoom level
     */
    private double latToTile(double lat) {
        double r = Math.toRadians(lat);
        return (1.0 - Math.log(Math.tan(r) + 1.0 / Math.cos(r)) / Math.PI) / 2.0 * Math.pow(2, zoom);
    }

    /**
     * This get the color for a apartment marker based on status
     * @param status the apartment status
     * @return the Color to use for the marker
     *
     * pre-condition: status is not null
     * post-condition: return correct color for the status
     */
    private Color getMarkerColor(Status status) {
        if (status == Status.NEW) return Color.web("#2196f3");
        if (status == Status.SHORTLISTED) return Color.web("#4caf50");
        if (status == Status.TOURED) return Color.web("#ff9800");
        return Color.web("#f44336");
    }

    /**
     * This center the map on a specific location
     * @param lat latitude
     * @param lon longitude
     *
     * pre-condition: valid coordinates
     * post-condition: map is redrawn centered there
     */
    public void centerOn(double lat, double lon) {
        centerLat = lat;
        centerLon = lon;
        zoom = 15;
        drawTiles();
    }

    /**
     * This refresh the map and recenter on average of all apartment
     * It calculate the middle point of all apartment with coordinates
     *
     * pre-condition: service is not null
     * post-condition: map is redrawn centered on apartments
     */
    public void refresh() {
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

    /**
     * This return the content VBox so other class can use it
     * @return the VBox that hold everything in this tab
     *
     * pre-condition: content is not null
     * post-condition: return the content VBox
     */
    public VBox getContent() {
        return content;
    }
}
