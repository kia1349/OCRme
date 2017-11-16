package com.ashomok.imagetotext.language_choser_mvp_di;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.IntStream;
import com.annimon.stream.Stream;
import com.ashomok.imagetotext.R;
import com.ashomok.imagetotext.Settings;
import com.ashomok.imagetotext.utils.LogUtil;
import com.ashomok.imagetotext.utils.SharedPreferencesUtil;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static com.ashomok.imagetotext.utils.InfoSnackbarUtil.showError;
import static com.ashomok.imagetotext.utils.LogUtil.DEV_TAG;

/**
 * Created by iuliia on 10/22/17.
 */

//MINOR todo add search view https://developer.android.com/training/search/search.html (add add async loader firstly because of technical reasons)
//MINOR todo add async loader for fill recyclerviews LoaderManager.LoaderCallbacks<List<String>>
//todo may be use rx for manipulation with two adapters
public class LanguageOcrActivity extends AppCompatActivity {
    private static final String TAG = DEV_TAG + LanguageOcrActivity.class.getSimpleName();
    public static final String CHECKED_LANGUAGE_CODES = "checked_languages_set";

    private List<String> recentlyChosenLanguageCodes;
    private boolean isAuto;
    private LanguagesListAdapter.ResponsableList<String> checkedLanguageCodes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_ocr);

        initToolbar();

        @Nullable List<String> list = obtainCheckedLanguageCodes();
        checkedLanguageCodes = (list == null) ?
                new LanguagesListAdapter.ResponsableList<>(new ArrayList<>())
                : new LanguagesListAdapter.ResponsableList<>(list);

        StateChangedNotifier notifier = isAutoChecked -> {
            if (!isAutoChecked) {
                isAuto = false;
                updateAutoUi(isAuto);
            }
        };

        //todo move to presenter
        //init recently chosen language list
        recentlyChosenLanguageCodes = obtainRecentlyChosenLanguageCodes();
        LanguagesListAdapter recentlyChosenLangAdapter = null;
        if (recentlyChosenLanguageCodes.size() > 0) {
            View recentlyChosen = findViewById(R.id.recently_chosen);
            recentlyChosen.setVisibility(View.VISIBLE);
            RecyclerView recyclerViewRecentlyChosen = findViewById(R.id.recently_chosen_list);
            recyclerViewRecentlyChosen.setHasFixedSize(true);
            LinearLayoutManager recentlyChosenLayoutManager = new LinearLayoutManager(this);
            recyclerViewRecentlyChosen.setLayoutManager(recentlyChosenLayoutManager);

            recentlyChosenLangAdapter = new LanguagesListAdapter(
                    recentlyChosenLanguageCodes, checkedLanguageCodes, notifier);
            recyclerViewRecentlyChosen.setAdapter(recentlyChosenLangAdapter);

        }

        //todo move to presenter
        //init all languages list
        RecyclerView recyclerViewAllLanguages = findViewById(R.id.all_languages_list);
        recyclerViewAllLanguages.setHasFixedSize(true);
        LinearLayoutManager allLanguagesLayoutManager = new LinearLayoutManager(this);
        recyclerViewAllLanguages.setLayoutManager(allLanguagesLayoutManager);
        List<String> allLanguageCodes = obtainAllLanguageCodes();
        LanguagesListAdapter allLangAdapter = new LanguagesListAdapter(
                allLanguageCodes, checkedLanguageCodes, notifier);
        recyclerViewAllLanguages.setAdapter(allLangAdapter);

        //init auto btn
        if (checkedLanguageCodes.size() < 1) {
            //check auto btn
            isAuto = true;
        }
        LinearLayout autoBtn = findViewById(R.id.auto);
        updateAutoUi(isAuto);

        LanguagesListAdapter finalRecentlyChosenLangAdapter = recentlyChosenLangAdapter;
        autoBtn.setOnClickListener(view -> {
            isAuto = !isAuto;
            updateAutoUi(isAuto);

            if (finalRecentlyChosenLangAdapter != null) {
                finalRecentlyChosenLangAdapter.onAutoStateChanged(isAuto);
            }
            allLangAdapter.onAutoStateChanged(isAuto);
        });
    }


//todo to presenter
    private List<String> obtainAllLanguageCodes() {
        return new ArrayList<>(Settings.getOcrLanguageSupportList(this).keySet());
    }

    /**
     * obtain recently chosen Languages from SharedPreferences in order: first - the most recently chosen.
     * Max 5 recently chosen Languages allowed.
     *
     * @return recently chosen Languages
     */
    //todo to presenter
    private
    List<String> obtainRecentlyChosenLanguageCodes() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE); //todo inject
        List<String> recentlyChosenLanguageCodes = SharedPreferencesUtil.pullStringList(
                sharedPref, getString(R.string.recently_chosen_languge_codes));
        if (recentlyChosenLanguageCodes == null) {
            recentlyChosenLanguageCodes = new ArrayList<>();
        }
        return recentlyChosenLanguageCodes;
    }

    /**
     * if returns null - auto detection is checked
     *
     * @return checked language keys or null, which means auto detection is checked
     */
    private @Nullable
    List<String> obtainCheckedLanguageCodes() {
        Intent intent = getIntent();
        ArrayList<String> extra = intent.getStringArrayListExtra(CHECKED_LANGUAGE_CODES);
        if (extra != null) {
            return extra;
        } else {
            return null;
        }
    }

    /**
     * call before finish activity
     */
    private void saveRecentlyChosenLanguages() {
        LinkedHashSet<String> languagesSet = new LinkedHashSet<>();
        languagesSet.addAll(checkedLanguageCodes);
        languagesSet.addAll(recentlyChosenLanguageCodes);

        List<String> languagesSubList =
                Stream.of(languagesSet).limit(5).collect(Collectors.toList());

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferencesUtil.pushStringList(sharedPref,
                languagesSubList, getString(R.string.recently_chosen_languge_codes));
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            //back btn pressed
            Intent intent = new Intent();
            saveRecentlyChosenLanguages();
            if (checkedLanguageCodes != null) {
                intent.putExtra(CHECKED_LANGUAGE_CODES, checkedLanguageCodes);
                setResult(RESULT_OK, intent);
            }

            finish();
        });
    }

    //todo to presenter
    void updateAutoUi(boolean checked) {
        View checkedIcon = findViewById(R.id.checked_icon);
        checkedIcon.setVisibility(checked ? View.VISIBLE : View.GONE);
        View autoIcon = findViewById(R.id.auto_icon);
        autoIcon.setVisibility(checked ? View.GONE : View.VISIBLE);
    }

    public interface StateChangedNotifier {
        void changeAutoState(boolean isAutoChecked);
    }

    /**
     * Created by iuliia on 12/11/16.
     */

    public static class LanguagesListAdapter extends RecyclerView.Adapter<LanguagesListAdapter.ViewHolder> {

        private static final String TAG = DEV_TAG + LanguagesListAdapter.class.getSimpleName();
        private static final int MAX_CHECKED_ALLOWED = 3;
        private final StateChangedNotifier notifier;
        private List<String> allLanguageCodes;
        private ResponsableList<String> checkedLanguageCodes;
        private PublishSubject<Integer> mViewClickSubject = PublishSubject.create();

        LanguagesListAdapter(List<String> allLanguageCodes,
                             @Nullable ResponsableList<String> checkedLanguageCodes,
                             StateChangedNotifier notifier) {

            this.allLanguageCodes = allLanguageCodes;
            this.notifier = notifier;

            this.checkedLanguageCodes = (checkedLanguageCodes == null) ?
                    new ResponsableList<>(new ArrayList<>()) : checkedLanguageCodes;
            this.checkedLanguageCodes.addOnListChangedListener(o -> {
                String checkedLanguage = (String) o;

                int changedPos = IntStream.range(0, allLanguageCodes.size())
                        .filter(i -> checkedLanguage.equals(allLanguageCodes.get(i)))
                        .findFirst().orElse(-1);

                notifyItemChanged(changedPos);
            });
        }

        List<String> getCheckedLanguageCodes() {
            return checkedLanguageCodes;
        }

        private void addToChecked(String language) {
            if (checkedLanguageCodes.size() < MAX_CHECKED_ALLOWED) {
                checkedLanguageCodes.add(language);
            } else {
                Log.w(TAG, "attempt to add checked language when max amount reached");
            }

            if (checkedLanguageCodes.size() > 0) {
                notifier.changeAutoState(false);
            }
        }

        void onAutoStateChanged(boolean isAutoChecked) {
            if (isAutoChecked) {
                //uncheck all items
                checkedLanguageCodes.clear();
                notifyDataSetChanged();
            }
        }

        private void removeFromChecked(String language) {
            checkedLanguageCodes.remove(language);
        }


        String getItem(int i) {
            return allLanguageCodes.get(i);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.ocr_language_row, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String item = getItem(position);
            View parent = holder.languageLayout.getRootView();

            holder.languageName.setText(Settings.getOcrLanguageSupportList(parent.getContext()).get(item));

            holder.languageLayout.setOnClickListener(view -> {
                if (checkedLanguageCodes.contains(item)) {
                    //checked - uncheck
                    removeFromChecked(item);
                    holder.updateUi(false);
                } else {
                    //unchecked - check
                    if (checkedLanguageCodes.size() < MAX_CHECKED_ALLOWED) {
                        addToChecked(item);
                        holder.updateUi(true);
                    } else {
                        String message = String.format(view.getContext().getString(R.string.max_checked_allowed),
                                String.valueOf(MAX_CHECKED_ALLOWED));
                        showError(message, parent);
                    }
                }
            });

            holder.updateUi(checkedLanguageCodes.contains(item));
        }

        @Override
        public int getItemCount() {
            return allLanguageCodes.size();
        }

        // Provide a reference to the views for each data item
        static class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout languageLayout;
            ImageView checkedIcon;
            TextView languageName;
            ImageView add;
            ImageView remove;

            ViewHolder(View v) {
                super(v);
                checkedIcon = v.findViewById(R.id.checked_icon);
                languageName = v.findViewById(R.id.language_name);
                add = v.findViewById(R.id.add);
                remove = v.findViewById(R.id.remove);
                languageLayout = v.findViewById(R.id.ocr_language_layout);
            }

            void updateUi(boolean checked) {
                checkedIcon.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
                add.setVisibility(checked ? View.GONE : View.VISIBLE);
                remove.setVisibility(checked ? View.VISIBLE : View.GONE);
            }
        }

        /**
         * list with add / remove element event
         *
         * @param <E>
         */
        static class ResponsableList<E> extends ArrayList<E> {

            private List<OnListChangedListener> listenerList = new ArrayList<>();

            ResponsableList(@NonNull Collection<? extends E> c) {
                super(c);
            }

            void addOnListChangedListener(OnListChangedListener listener) {
                listenerList.add(listener);

            }

            @Override
            public boolean add(E e) {
                for (OnListChangedListener listener : listenerList) {
                    listener.onListChangedFor(e);
                }
                return super.add(e);
            }

            @Override
            public boolean remove(Object o) {
                for (OnListChangedListener listener : listenerList) {
                    listener.onListChangedFor(o);
                }
                return super.remove(o);
            }
        }

        public interface OnListChangedListener {
            void onListChangedFor(Object o);
        }
    }
}