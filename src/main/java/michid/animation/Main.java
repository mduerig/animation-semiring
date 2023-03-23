package michid.animation;

import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.RED;
import static javafx.util.Duration.millis;
import static michid.animation.Animation.par;
import static michid.animation.Animation.seq;
import static michid.animation.Easing.easeInOut;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final int RADIUS = 20;
    private double t = 0;

    @Override
    public void start(Stage stage) {
        Circle circle = new Circle(RADIUS, BLUE);
        circle.setTranslateY(HEIGHT/2);

        Rectangle square = new Rectangle(RADIUS, RADIUS, RED);
        square.setTranslateX(WIDTH/2);

        var translateCircle = Animation
            .linear(1000, 20, 580)
            .bind(circle::setTranslateX);

        var growCircle = Animation
            .linear(1000, 5, 50)
            .bind(circle::setRadius);

        var translateSquare = Animation
            .linear(1000, 20, 380)
            .bind(square::setTranslateY);

        var growSquare = Animation
            .linear(1000, 5, 50)
            .bind(l -> {
                square.setWidth(l);
                square.setHeight(l);
            });

        var animation =
            seq(
                par(translateSquare, growSquare)
                    .ease(easeInOut(1.5)),
                par(translateCircle, growCircle)
                    .ease(easeInOut(1.5)))
            .loop();

        Timeline timeline = new Timeline(new KeyFrame(millis(16), e -> {
            animation
                .repeat(5)
                .render(t);

            t += 0.001;
        }));

        timeline.setCycleCount(1000);
        timeline.play();

        stage.setScene(new Scene(new Pane(circle, square), WIDTH, HEIGHT));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
