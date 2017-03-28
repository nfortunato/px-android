package com.mercadopago;

import android.content.Intent;
import android.os.Bundle;

import com.mercadopago.core.MercadoPagoComponents;
import com.mercadopago.model.FinancialInstitution;
import com.mercadopago.model.Identification;
import com.mercadopago.model.IdentificationType;
import com.mercadopago.model.PaymentMethod;
import com.mercadopago.model.Site;
import com.mercadopago.preferences.DecorationPreference;
import com.mercadopago.presenters.AdditionalStepVaultPresenter;
import com.mercadopago.providers.AdditionalStepVaultProviderImpl;
import com.mercadopago.util.ErrorUtil;
import com.mercadopago.util.JsonUtil;
import com.mercadopago.views.AdditionalStepVaultActivityView;


/**
 * Created by marlanti on 3/23/17.
 */

public class AdditionalStepVaultActivity extends MercadoPagoBaseActivity implements AdditionalStepVaultActivityView {

    private AdditionalStepVaultPresenter mPresenter;
    private DecorationPreference mDecorationPreference;
    private Identification mSelectedIdentification;
    private IdentificationType mSelectedIdentificationType;
    private FinancialInstitution mSelectedFinancialInstitution;
    private String mSelectedEntityType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createPresenter();
        getActivityParameters();

        mPresenter.attachView(this);
        mPresenter.attachResourcesProvider(new AdditionalStepVaultProviderImpl(this));
        mPresenter.validateActivityParameters();


    }

    private void createPresenter() {
        mPresenter = new AdditionalStepVaultPresenter();
    }


    private void getActivityParameters() {

        String publicKey = getIntent().getStringExtra("merchantPublicKey");
        mDecorationPreference = JsonUtil.getInstance().fromJson(getIntent().getStringExtra("decorationPreference"), DecorationPreference.class);

        PaymentMethod paymentMethod = JsonUtil.getInstance().fromJson(
                this.getIntent().getStringExtra("paymentMethod"), PaymentMethod.class);

        Site site = JsonUtil.getInstance().fromJson(this.getIntent().getStringExtra("site"), Site.class);


        mPresenter.setSite(site);
        mPresenter.setPaymentMethod(paymentMethod);
        mPresenter.setPublicKey(publicKey);

    }


    @Override
    public void onInvalidStart(String msg) {
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onValidStart() {
        mPresenter.checkFlow();
    }


    public void startEntityTypeStep() {

        new MercadoPagoComponents.Activities.EntityTypeActivityBuilder()
                .setActivity(this)
                .setMerchantPublicKey(mPresenter.getmPublicKey())
                .setPaymentMethod(mPresenter.getmPaymentMethod())
                .setIdentification(mSelectedIdentification)
                .setIdentificationType(mSelectedIdentificationType)
                .setDecorationPreference(mDecorationPreference)
                .setSite(mPresenter.getSite())
                .startActivity();

        animatePaymentMethodSelection();

    }

    public void startIdentificationStep() {

        new MercadoPagoComponents.Activities.IdentificationActivityBuilder()
                .setActivity(this)
                .setMerchantPublicKey(mPresenter.getmPublicKey())
                .setDecorationPreference(mDecorationPreference)
                .startActivity();

        animatePaymentMethodSelection();

    }

    public void startFinancialInstitutionsStep() {

        new MercadoPagoComponents.Activities.FinancialInstitutionsActivityBuilder()
                .setActivity(this)
                .setMerchantPublicKey(mPresenter.getmPublicKey())
                .setPaymentMethod(mPresenter.getmPaymentMethod())
                .setDecorationPreference(mDecorationPreference)
                .startActivity();

        animatePaymentMethodSelection();

    }

    private void animatePaymentMethodSelection() {
        overridePendingTransition(R.anim.mpsdk_slide_right_to_left_in, R.anim.mpsdk_slide_right_to_left_out);
    }

    private void resolveFinancialInstitutionsRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            this.mSelectedFinancialInstitution = JsonUtil.getInstance().fromJson(data.getStringExtra("financialInstitution"), FinancialInstitution.class);
            mPresenter.checkFlowWithFinancialInstitutionSelected();

        } else if (resultCode == RESULT_CANCELED) {
            mPresenter.onBackPressed();
        }
    }

    @Override
    public void finishWithResult() {
        //TODO
        Intent returnIntent = new Intent();
        /*returnIntent.putExtra("payer", payer);
        returnIntent.putExtra("paymentData", paymentData);*/
        setResult(RESULT_OK, returnIntent);
        finish();
        overridePendingTransition(R.anim.mpsdk_hold, R.anim.mpsdk_hold);
    }

    private void resolveIdentificationRequest(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            this.mSelectedIdentification = JsonUtil.getInstance().fromJson(data.getStringExtra("identification"), Identification.class);
            this.mSelectedIdentificationType = JsonUtil.getInstance().fromJson(data.getStringExtra("identificationType"), IdentificationType.class);

            mPresenter.checkFlowWithIdentificationSelected();

        } else if (resultCode == RESULT_CANCELED) {
            mPresenter.onBackPressed();
        }
    }

    private void resolveEntityTypeRequest(int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            this.mSelectedEntityType = data.getStringExtra("entityType");

            mPresenter.checkFlowWithEntityTypeSelected();

        } else if (resultCode == RESULT_CANCELED) {
            mPresenter.onBackPressed();
        }
    }

    @Override
    public void finishWithCancel() {
        setResult(RESULT_CANCELED);
        this.finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == MercadoPagoComponents.Activities.FINANCIAL_INSTITUTIONS_REQUEST_CODE) {
            resolveFinancialInstitutionsRequest(resultCode, data);

        } else if (requestCode == MercadoPagoComponents.Activities.IDENTIFICATION_REQUEST_CODE)

        {
            resolveIdentificationRequest(resultCode, data);
        } else if (requestCode == MercadoPagoComponents.Activities.ENTITY_TYPE_REQUEST_CODE)

        {
            resolveEntityTypeRequest(resultCode, data);

        }

    }

    @Override
    public void onError(String message) {
        ErrorUtil.startErrorActivity(this, message, false);
    }
}