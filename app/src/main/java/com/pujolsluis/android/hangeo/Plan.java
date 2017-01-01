package com.pujolsluis.android.hangeo;

import java.util.Random;

/**
 * Created by Luis on 12/18/2016.
 */

public class Plan {
    private static final Random RANDOM = new Random();

    public static int getRandomCheeseDrawable() {
        switch (RANDOM.nextInt(7)) {
            default:
            case 0:
                return R.drawable.plan_example_1;
            case 1:
                return R.drawable.plan_example_2;
            case 2:
                return R.drawable.plan_example_3;
            case 3:
                return R.drawable.plan_example_4;
            case 4:
                return R.drawable.plan_example_5;
            case 5:
                return R.drawable.example_1;
            case 6:
                return R.drawable.example_3;
        }
    }

    public static final String[] sPlanStrings = {
            "A Night in the Clouds","A Night of Mystery","A Night on Treasure Island",
            "Bright Lights, Big City", "Broadway Backstage", "Can’t Fight the Moonlight",
            "Captured in a Dream", "Forever Tonight", "From this Moment", "Garden of Enchantment",
            "Heaven in Your Eyes", "Heaven on Earth", "Here’s to the Night", "It’s a Jungle Out There",
            "Jungle Allure", "Let the Good Times Roll", "Magical Memories", "Mystical Journey",
            "Mystical Twilight", "New York, New York", "Night in New Orleans", "Parisian Romance/Escape",
            "Putting on the Glitz/Ritz", "Remember Me Always", "Retro Romance", "Roaring Twenties",
            "Space Odyssey", "Springtime in Paris", "Stand By Me", "Star Struck", "Voices That Care",
            "Waiting for Tonight", "What Dreams May Come"
    };

}
