package application.controller

import application.Main
import application.backend.convertToImage
import application.gui.NodesPane
import application.model.MediaControl
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import org.apache.poi.ss.formula.functions.NumericFunction.LOG
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.JavaFXFrameConverter
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_videoio.VideoCapture
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ShortBuffer
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine


class TabController: Initializable {
    companion object {
        var panes = HashMap<VBox, TabController>()

        fun getController(node: Node?): TabController? {
            return if (panes.containsKey(node)) panes[node] else null
        }
    }

    @FXML lateinit var pane: Pane
    @FXML lateinit var nodesPane: NodesPane
    @FXML lateinit var imageView: ImageView
    @FXML lateinit var imageViewOrig: ImageView
    @FXML lateinit var parent: VBox

    lateinit var resource: String

    // lateinit var media: Parent

    var playThread: Thread? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        panes[parent] = this
        resource = Objects.requireNonNull(Main::class.java.getResource("/video/Untitledd.mp4")).toExternalForm()
        // play()
        // media = createContent()
        // pane.children.add(media)
    }

    fun setFile(filename: String) {
        resource = filename
//        Platform.runLater {
//            //pane.children.remove(media)
//            //media = createContent()
//            //pane.children.add(media)
//        }

        playThread?.interrupt()

    }

    fun play() {
        println("Button has been pressed!")
        stop()
        playThread = Thread {
            val video = VideoCapture(resource)
            val img = Mat()

            val postprocessor = nodesPane.postprocessor
            if(postprocessor != null) {
                println(postprocessor.process(sequence { while (video.read(img)) yield(nodesPane.preprocessor.process(img)) }))
            } else {
                whileLoop@ while (video.read(img) && !Thread.interrupted()) {
                    println("An Image Loaded")
                    Platform.runLater {
                        try {
                            imageViewOrig.image = convertToImage(img)
                            imageView.image = convertToImage(nodesPane.preprocessor.process(img))
                        } catch(e: RuntimeException) {}
                    }
                    Thread.sleep(1000)
                }
            }
            Platform.exit()

        }
        playThread?.start()
    }

    fun createContent(): Parent {
        val mediaPlayer = MediaPlayer(Media(resource)).apply { isAutoPlay = true }
        val mediaControl = MediaControl(mediaPlayer)
        mediaControl.setMinSize(800.0, 467.0)
        mediaControl.setPrefSize(800.0, 467.0)
        mediaControl.setMaxSize(800.0, 467.0)
        return mediaControl
    }

    fun stop() {
        if(playThread != null && playThread!!.isAlive) playThread!!.interrupt()
    }
}