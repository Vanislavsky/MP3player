package sample;

import com.jfoenix.controls.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;
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
import javafx.scene.layout.GridPane;
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
    private Label reverseCurTimeLabel;

    @FXML
    private JFXSlider musicSlider;

    @FXML
    private JFXSlider volumeSlider;

    @FXML
    private TableView<Music> musicsTable;

    @FXML
    private MenuItem select;

    @FXML
    private MenuItem exitItem;

    @FXML
    private MenuItem deleteItem;

    @FXML
    private MenuItem aboutItem;

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
    String reverseCurTime = new String();

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
        numberColumn.setStyle("-fx-alignment: CENTER;");
        nameColumn.setCellValueFactory(new PropertyValueFactory<Music, String>("name"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<Music, String>("refactorPath"));
        pathColumn.prefWidthProperty().bind(
                musicsTable.widthProperty()
                .subtract(numberColumn.widthProperty())
                .subtract(nameColumn.widthProperty())
                .subtract(2)
        );

        fileChooser = new FileChooser();

        exitItem.setOnAction(actionEvent -> {
            System.exit(0);
        });

        deleteItem.setOnAction(actionEvent -> {
            musicsTable.getItems().clear();
            mediaPlayer = null;
            indexCurTrack = -1;
        });

        aboutItem.setOnAction(actionEvent -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("about app");
            alert.setHeaderText("This is simple mp3player. Select file -> select to select a song." +
                    " To play, just click on the song of your choice.");
            alert.show();
        });

        select.setOnAction(actionEvent -> {
            List<File> files = fileChooser.showOpenMultipleDialog(select.getParentPopup().getScene().getWindow());
            for(var file: files) {
                if(file != null) {
                    musicsTable.getItems().addAll(new Music(musicsTable.getItems().size() + 1, file.getName(),file.toURI().toString()));
                }
            }
        });

        musicSlider.valueProperty().addListener(musicTimeListener);
        volumeSlider.valueProperty().addListener(volumeListener);

        TableView.TableViewSelectionModel<Music> selectionModel = musicsTable.getSelectionModel();
        selectionModel.selectedItemProperty().addListener(tablelistener);

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

    void SetReverseTime() {
        var sec = ((int)mediaPlayer.getMedia().getDuration().toSeconds()) % 60 - ((int)mediaPlayer.getCurrentTime().toSeconds()) % 60;
        var min = (int) mediaPlayer.getMedia().getDuration().toMinutes() - (int) mediaPlayer.getCurrentTime().toMinutes();
        if(sec < 10) {
            reverseCurTime = "-" + String.valueOf(min) + ":0" + String.valueOf(sec);
        } else {
            reverseCurTime = "-" + String.valueOf(min) + ":" + String.valueOf(sec);
        }
        reverseCurTimeLabel.setText(reverseCurTime);
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

    ChangeListener<Duration> curTimeListener = new ChangeListener<Duration>() {
        @Override
        public void changed(ObservableValue<? extends Duration> observableValue, Duration duration, Duration t1) {
            musicSlider.setValue(mediaPlayer.getCurrentTime().toSeconds());
            SetTime();
            SetReverseTime();
        }
    };

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
        mediaPlayer.currentTimeProperty().addListener(curTimeListener);
        musicsTable.setRowFactory(new StyleRowFactory<Music>());
        musicsTable.refresh();
    }

    ChangeListener<Music> tablelistener = new ChangeListener<Music>() {
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
    };

    InvalidationListener musicTimeListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            if (musicSlider.isPressed()) {
                mediaPlayer.seek(Duration.seconds(musicSlider.getValue()));
            }
        }
    };

    InvalidationListener volumeListener =  new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
        }
    };
}

