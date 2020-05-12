package net.haesleinhuepf.clijx.te_oki;

import net.haesleinhuepf.clijx.jython.ScriptingAutoCompleteProvider;
import org.fife.ui.autocomplete.BasicCompletion;

import java.util.ArrayList;

public class NapariAutoComplete {
    public static ArrayList<BasicCompletion> getCompletions(final ScriptingAutoCompleteProvider provider) {
        ArrayList<BasicCompletion> list = new ArrayList<BasicCompletion>();
        String headline;
        String description;
        headline = "viewer.add_image(image);\n";
        description = "<b>add_image</b><br><br>Add an image to the Napari viewer.";
        list.add(new BasicCompletion(provider, headline, null, description));

        headline = "viewer.add_image(image, name='title');\n";
        description = "<b>add_image</b><br><br>Add an image with a given layer title to the Napari viewer.";
        list.add(new BasicCompletion(provider, headline, null, description));

        headline = "viewer.add_image(image, opacity=0.5);\n";
        description = "<b>add_image</b><br><br>Add an image with a given opacity to the Napari viewer.";
        list.add(new BasicCompletion(provider, headline, null, description));

        headline = "viewer.add_image(image, colormap='red');\n";
        description = "<b>add_image</b><br><br>Add an image with a given lookup table to the Napari viewer.";
        list.add(new BasicCompletion(provider, headline, null, description));

        headline = "viewer.add_image(image, contrast_limits=(lower, upper));\n";
        description = "<b>add_image</b><br><br>Add an image with given contrast limits to the Napari viewer.";
        list.add(new BasicCompletion(provider, headline, null, description));

        headline = "viewer.add_points(points, size=30);\n";
        description = "<b>add_points</b><br><br>Add an image with given size to the Napari viewer.";
        list.add(new BasicCompletion(provider, headline, null, description));

        headline = "viewer.add_points(points, size=30);\n";
        description = "<b>add_points</b><br><br>Add an image with given size to the Napari viewer.";
        list.add(new BasicCompletion(provider, headline, null, description));

        headline = "viewer.theme = 'light';\n";
        description = "<b>viewer.theme</b><br><br>Change the theme of the Napari viewer.";
        list.add(new BasicCompletion(provider, headline, null, description));

        headline = "viewer.theme = 'dark';\n";
        description = "<b>viewer.theme</b><br><br>Change the theme of the Napari viewer.";
        list.add(new BasicCompletion(provider, headline, null, description));


        return list;
    }
}
