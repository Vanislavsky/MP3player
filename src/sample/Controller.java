package sample;

import com.jfoenix.controls.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;

public class Controller {
    
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private JFXButton playButton;

    @FXML
    private JFXButton nextButton;

    @FXML
    private JFXButton prevButton;

    @FXML
    private Label curTimeLabel;

    @FXML
    private JFXSlider musicSlider;

    @FXML
    private JFXSlider volumeSlider;

    @FXML
    private TableView<Music> musicsTable;

    @FXML
    private MenuItem select;

    @FXML
    private TableColumn<Music, Integer> numberColumn;

    @FXML
    private TableColumn<Music, String> nameColumn;

    @FXML
    private TableColumn<Music, String> pathColumn;
    FileChooser fileChooser;
    MediaPlayer mediaPlayer = null;
    int indexCurTrack = -1;
    String curTime = new String();

    @FXML
    void initialize() {
        try {
            playButton.setGraphic(new ImageView(new Image(new FileInputStream("src/icons/play.png"))));
            nextButton.setGraphic(new ImageView(new Image(new FileInputStream("src/icons/next2.png"))));
            prevButton.setGraphic(new ImageView(new Image(new FileInputStream("src/icons/prev2.png"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        numberColumn.setCellValueFactory(new PropertyValueFactory<Music, Integer>("number"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<Music, String>("path"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<Music, String>("name"));
        fileChooser = new FileChooser();
        select.setOnAction(actionEvent -> {
            var file =fileChooser.showOpenDialog(select.getParentPopup().getScene().getWindow());
            if(file != null) {
                musicsTable.getItems().addAll(new Music(musicsTable.getItems().size() + 1,file.getName(),file.toURI().toString()));
            }
        });

        TableView.TableViewSelectionModel<Music> selectionModel = musicsTable.getSelectionModel();
        selectionModel.selectedItemProperty().addListener(new ChangeListener<Music>() {
            public void changed(ObservableValue<? extends Music> val, Music oldVal, Music newVal) {
                if (newVal != null) {
                    Play(newVal.getPath());
                    try {
                        playButton.setGraphic(new ImageView(new Image(new FileInputStream("src/icons/pause.png"))));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    indexCurTrack = newVal.number - 1;
                }
                }
            });


        musicSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if(musicSlider.isPressed()) {
                    mediaPlayer.seek(Duration.seconds(musicSlider.getValue()));
                }
            }
        });


        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            }
        });

        playButton.setOnAction(actionEvent -> {
            if(mediaPlayer != null){
                var status = mediaPlayer.getStatus();
                if(status == MediaPlayer.Status.PAUSED) {
                    mediaPlayer.play();
                    try {
                        playButton.setGraphic(new ImageView(new Image(new FileInputStream("src/icons/pause.png"))));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    mediaPlayer.pause();
                    try {
                        playButton.setGraphic(new ImageView(new Image(new FileInputStream("src/icons/play.png"))));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        nextButton.setOnAction(actionEvent -> {
            if(indexCurTrack == -1)
                return;
            indexCurTrack++;
            if(indexCurTrack == musicsTable.getItems().size())
                indexCurTrack = 0;
            Play(musicsTable.getItems().get(indexCurTrack).getPath());
            try {
                playButton.setGraphic(new ImageView(new Image(new FileInputStream("src/icons/pause.png"))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        prevButton.setOnAction(actionEvent -> {
            if(indexCurTrack == -1)
                return;
            indexCurTrack--;
            if(indexCurTrack < 0)
                indexCurTrack  = musicsTable.getItems().size() - 1;
            Play(musicsTable.getItems().get(indexCurTrack).getPath());
            try {
                playButton.setGraphic(new ImageView(new Image(new FileInputStream("src/icons/pause.png"))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    void SetTime() {
        var sec = ((int)mediaPlayer.getCurrentTime().toSeconds()) % 60;
        if(sec < 10) {
            curTime = String.valueOf((int) mediaPlayer.getCurrentTime().toMinutes()) + ":0" + String.valueOf(sec);
        } else {
            curTime = String.valueOf((int) mediaPlayer.getCurrentTime().toMinutes()) + ":" + String.valueOf(sec);
        }
        curTimeLabel.setText(curTime);
    }

    class StyleRowFactory<T> implements Callback<TableView<T>, TableRow<T>> {
        @Override
        public TableRow<T> call(TableView<T> tableView) {
            return new TableRow<T>() {
                @Override
                protected void updateItem( T paramT, boolean b ) {
                    if(getIndex() == indexCurTrack) {
                        setStyle("-fx-background-color: #04B404;-fx-text-background-color: white;");

                    } else {
                        setStyle(null);
                    }
                    super.updateItem(paramT, b);
                }
            };
        }

    }

    void Play(String path) {
        if (mediaPlayer != null)
            mediaPlayer.stop();
        mediaPlayer = new MediaPlayer(new Media(path));
        mediaPlayer.play();
        mediaPlayer.setOnReady(() -> {
            musicSlider.setMin(0);
            musicSlider.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
            musicSlider.setValue(0);
        });
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration t1) {
                musicSlider.setValue(mediaPlayer.getCurrentTime().toSeconds());
                SetTime();
            }
        });
        musicsTable.setRowFactory(new StyleRowFactory<Music>());
        musicsTable.refresh();
    }
}

