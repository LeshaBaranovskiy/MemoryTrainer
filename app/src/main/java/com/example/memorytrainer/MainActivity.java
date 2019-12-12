package com.example.memorytrainer;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Handler lHandler = new Handler();
    private Handler l1Handler = new Handler();

    private ObjectAnimator oa1;
    private ObjectAnimator oa2;

    private TextView currentFlippedCard;
    private TextView firstFlippedCard;
    private TextView firstPlayerScoreTextView;
    private TextView secondPlayerScoreTextView;

    private ImageView imageViewFirstPlayerScore;
    private ImageView imageViewSecondPlayerScore;

    private ArrayList<Integer> randomNumbers = new ArrayList<>();
    private ArrayList<TextView> textViews = new ArrayList<>();
    private ArrayList<TextView> textViewsBlocked = new ArrayList<>();
    private ArrayList<String> drawableNames = new ArrayList<>();

    private int turn = 0;
    private int firstPlayerScore = 0;
    private int secondPlayerScore = 0;
    private int cardsFlipped = 0;

    private String firstCardImageName = "";
    private String secondCardImageName = "";

    private boolean chooseCorrect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        firstPlayerScoreTextView = findViewById(R.id.textViewFirstPlayerScore);
        secondPlayerScoreTextView = findViewById(R.id.textViewSecondPlayerScore);
        imageViewFirstPlayerScore = findViewById(R.id.FirstPlayerImageView);
        imageViewSecondPlayerScore = findViewById(R.id.SecondPlayerImageView);

        //Заносим все textView в массив
        for (int i = 1; i <= 16; i++) {
            currentFlippedCard = findViewById(getResources().getIdentifier("textViewCard" + i, "id", getPackageName()));
            textViews.add(currentFlippedCard);
        }

        randomCards();
        turnAnimation(turn);

        //Создаем массив из названий картинок, которые находятся в папке drawable
        //в случайном порядке благодаря созданому ранее массиву в методе randomCards()
        for (int arrayNumber : randomNumbers) {
            if (arrayNumber >= 9) {
                arrayNumber -= 8;
            }
            String drawableName = "image_" + arrayNumber + "_48dp";
            drawableNames.add(drawableName);
        }
    }

    //Выполнение анимации при нажатии на одну из карт
    public void cardFlip(View view) {
        currentFlippedCard = findViewById(view.getId());

        cardsFlipped++;

        if (cardsFlipped == 1) {
            firstFlippedCard = currentFlippedCard;
            firstFlippedCard.setClickable(false);
        }

        cardAnimation(currentFlippedCard,  false);

        oa1.addListener(new AnimatorListenerAdapter() {

            //Тут мы вставляем нужную картинку в карту
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                int chosenTextViewIndex = 0;

                //Находим под каким номером карточка на которую было произведено
                //нажатие и заносим её в номер в переменную chosenTextViewIndex
                for (TextView textViewLoop: textViews) {
                    if (textViewLoop == currentFlippedCard) {
                        break;
                    } else {
                        chosenTextViewIndex++;
                    }
                }

                //В зависимости какая по счету карта сейчас перевернута, присваиваем переменной
                //имя карты
                if (firstCardImageName.isEmpty()) {
                    firstCardImageName = drawableNames.get(chosenTextViewIndex);
                } else {
                    secondCardImageName = drawableNames.get(chosenTextViewIndex);
                }


                //Вставляем картнику в нажатую карточку
                textViews.get(chosenTextViewIndex).setCompoundDrawablesWithIntrinsicBounds((getResources().
                        getIdentifier(drawableNames.get(chosenTextViewIndex), "drawable", getPackageName())), 0, 0, 0);
                oa2.start();

                isEqual(firstCardImageName, secondCardImageName);

                if(cardsFlipped == 2) {
                    firstCardImageName = "";
                    secondCardImageName = "";
                    cardsFlipped = 0;
                    turnAnimation(turn);
                }

            }
        });
        oa1.start();
    }

    //В этом методе выполняется проверка того, одинаковые ли карты
    //В случае, если они оказались одинаковые, они остаются перевернутыми
    //В случае, если они разные - карты одновременно переворачиваются с помощью потоков
    private void isEqual(String firstCardImageName, String secondCardImageName) {
        if (firstCardImageName.equals(secondCardImageName)) {

            textViewsBlocked.add(currentFlippedCard);
            textViewsBlocked.add(firstFlippedCard);


            if (turn % 2 != 0) {
                secondPlayerScore++;
                secondPlayerScoreTextView.setText(String.format("%s", secondPlayerScore));
            } else {
                firstPlayerScore++;
                firstPlayerScoreTextView.setText(String.format("%s", firstPlayerScore));
            }
        } else if (cardsFlipped == 2) {

            turn++;

            blockOrUnblockCards(false);

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    cardAnimation(currentFlippedCard, true);

                    oa1.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {

                            cardAnimation(firstFlippedCard, false);
                            firstFlippedCard.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            oa2.start();

                            blockOrUnblockCards(true);
                        }
                    });
                    oa1.start();
                }
            };
            lHandler.post(r);
        }
    }

    //Генерируют ArrayList из элементов от 1 до 16(без повторений)
    private void randomCards() {
        Random random = new Random();
        int i = 0;
        //Тут используется метка, так как нам надо будет
        //пропустить итерацию основного цикла через вложеный
        outer: while (i <= 15){
            int number = 1 + random.nextInt(16);
            for (int arrayNumber : randomNumbers) {
                if (arrayNumber == number) {
                    continue outer;
                }
            }
            randomNumbers.add(number);
            i++;
        }
    }

    //При переворачивании 2 карт заблокировать/разблокировать остальные карты
    private void blockOrUnblockCards(boolean block) {
        for (TextView textView: textViews) {
            textView.setClickable(block);
        }
        for (TextView textViewBlocked: textViewsBlocked) {
            textViewBlocked.setClickable(!block);
        }
    }

    //Анимация переворачивания карты
    private void cardAnimation(final TextView textView1, boolean needListener) {
        oa1 = ObjectAnimator.ofFloat(textView1, "scaleX", 1f, 0f);
        oa2 = ObjectAnimator.ofFloat(textView1, "scaleX", 0f, 1f);
        oa1.setInterpolator(new DecelerateInterpolator());
        oa2.setInterpolator(new AccelerateDecelerateInterpolator());
        oa1.setDuration(100);
        oa2.setDuration(100);
        if (needListener) {
            oa1.setStartDelay(1000);
            oa1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    textView1.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    oa2.start();
                }
            });
            oa1.start();
        }
    }

    private void turnAnimation(int turn) {

        ObjectAnimator imageFirst = ObjectAnimator.ofFloat(imageViewFirstPlayerScore, "translationY", 0f);
        ObjectAnimator imageSecond = ObjectAnimator.ofFloat(imageViewSecondPlayerScore, "translationY", 0f);
        ObjectAnimator textFirst = ObjectAnimator.ofFloat(firstPlayerScoreTextView, "translationY", 0f);
        ObjectAnimator textSecond = ObjectAnimator.ofFloat(secondPlayerScoreTextView, "translationY", 0f);

        if (turn == 0) {
            imageSecond.setFloatValues(-70f);
            textSecond.setFloatValues(-70f);
            imageSecond.start();
            textSecond.start();
        }
        else if (turn % 2 == 0) {
            imageFirst.setFloatValues(0f);
            textFirst.setFloatValues(0f);
            imageSecond.setFloatValues(-70f);
            textSecond.setFloatValues(-70f);

            imageFirst.setStartDelay(800);
            imageSecond.setStartDelay(800);
            textFirst.setStartDelay(800);
            textSecond.setStartDelay(800);

            imageFirst.start();
            textFirst.start();
            imageSecond.start();
            textSecond.start();
        } else {
            imageFirst.setFloatValues(-70f);
            textFirst.setFloatValues(-70f);
            imageSecond.setFloatValues(0f);
            textSecond.setFloatValues(0f);

            imageFirst.setStartDelay(800);
            imageSecond.setStartDelay(800);
            textFirst.setStartDelay(800);
            textSecond.setStartDelay(800);

            imageFirst.start();
            textFirst.start();
            imageSecond.start();
            textSecond.start();
        }
    }
}
