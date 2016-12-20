package com.ashomok.imagetotext;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.runner.AndroidJUnit4;

import com.ashomok.imagetotext.language.Language;
import com.ashomok.imagetotext.language.LanguageList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.ashomok.imagetotext.language.LanguageList.CHECKED_LANGUAGES;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by iuliia on 12/20/16.
 */
@RunWith(AndroidJUnit4.class)
public class LanguageListTest {

    private LanguageList instance;

    @Before
    public void setup() {
        instance = LanguageList.getInstance();
    }

    @Test
    public void putDataToSharedPreferancesTest() throws Exception {
        LinkedHashSet<Language> data = new LinkedHashSet<>();
        data.add(new Language("English", "eng"));
        data.add(new Language("Germany", "de"));

        instance.putDataToSharedPreferances(data);

        Context context = App.getContext();
        assertNotNull(context);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> checkedLanguagesNames = sharedPref.getStringSet(CHECKED_LANGUAGES, null);
        assertNotNull(checkedLanguagesNames);
        assertTrue(checkedLanguagesNames.size() == data.size());
    }



}
