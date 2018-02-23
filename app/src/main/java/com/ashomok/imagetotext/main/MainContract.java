package com.ashomok.imagetotext.main;

/**
 * Created by iuliia on 2/14/18.
 */

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.annimon.stream.Optional;
import com.ashomok.imagetotext.di_dagger.BasePresenter;
import com.ashomok.imagetotext.main.billing.BillingProviderCallback;
import com.ashomok.imagetotext.my_docs.MyDocsContract;
import com.ashomok.imagetotext.ocr.ocr_task.OcrResult;

import java.util.List;

/**
 * This specifies the contract between the view and the presenter.
 */
public class MainContract {
    interface View {

        void showError(@StringRes int errorMessageRes);

        void showInfo (@StringRes int infoMessageRes);

        void updateLanguageString(String languageString);

        void updateView(boolean isPremium);
    }

    interface Presenter extends BasePresenter<MainContract.View> {
        void onCheckedLanguageCodesObtained(@Nullable List<String> checkedLanguageCodes);

        Optional<List<String>> getLanguageCodes();
    }
}
