package com.example.capstone.posting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.capstone.BaseFragment;
import com.example.capstone.R;
import com.example.capstone.data.model.Posting;
import com.example.capstone.databinding.FragmentPostingBinding;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;


public class PostingFragment extends BaseFragment implements TextWatcher {

    public static final String TAG = "PostingFragment";

    private FragmentPostingBinding binding;
    private PostingViewModel viewModel;
    private String nickName;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPostingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PostingViewModel.class);

        ((AutoCompleteTextView) binding.foodClassificationTextField.getEditText()).addTextChangedListener(this);
        ((AutoCompleteTextView) binding.foodClassificationTextField.getEditText()).setAdapter(new ArrayAdapter<>(
                requireContext(),
                R.layout.item_list, getResources().getStringArray(R.array.food_classification)));

        binding.nameTextField.getEditText().addTextChangedListener(this);

        binding.minOrderAmountTextField.getEditText().addTextChangedListener(this);
        binding.minOrderAmountTextField.getEditText().setTransformationMethod(null);

        binding.deliveryFeeTextField.getEditText().addTextChangedListener(this);
        binding.deliveryFeeTextField.getEditText().setTransformationMethod(null);

        binding.contentsTextField.getEditText().addTextChangedListener(this);

        binding.postingButton.setOnClickListener(v -> posting());
    }

    private boolean getTextFieldValidation() {
        String foodClassification = ((AutoCompleteTextView) binding.foodClassificationTextField.getEditText())
                .getText().toString();
        if (foodClassification.isEmpty()) return false;

        String name = binding.nameTextField.getEditText().getText().toString().trim();
        if (name.isEmpty()) return false;

        String minOrderAmount = binding.minOrderAmountTextField.getEditText().getText().toString().trim();
        if (minOrderAmount.isEmpty()) return false;
        try {
            Integer.parseInt(minOrderAmount);
        } catch (Exception ignore) {
            return false;
        }

        String deliveryFee = binding.deliveryFeeTextField.getEditText().getText().toString().trim();
        if (deliveryFee.isEmpty()) return false;
        try {
            Integer.parseInt(deliveryFee);
        } catch (Exception ignore) {
            return false;
        }

        String contents = binding.contentsTextField.getEditText().getText().toString().trim();
        if (contents.isEmpty()) return false;

        return true;
    }

    private void posting() {
        if (binding.progressView.getVisibility() == View.VISIBLE) return;

        if (location == null) {
            showToastMessage("현재 위치를 찾을 수 없습니다. 잠시 후 다시 시도해 주세요.");
            return;
        }

        binding.progressView.setVisibility(View.VISIBLE);

        Posting posting = new Posting();
        posting.setUid(FirebaseAuth.getInstance().getUid());
        posting.setNickName(getCurrentUser().getDisplayName());
        posting.setLatitude(location.getLatitude());
        posting.setLongitude(location.getLongitude());
        posting.setGeoHash(GeoFireUtils.getGeoHashForLocation(
                new GeoLocation(location.getLatitude(), location.getLongitude())));
        posting.setFoodClassification(
                ((AutoCompleteTextView) binding.foodClassificationTextField.getEditText()).getText().toString());
        posting.setRestaurantName(binding.nameTextField.getEditText().getText().toString().trim());
        posting.setMinimumOrderAmount(
                Integer.parseInt(binding.minOrderAmountTextField.getEditText().getText().toString().trim()));
        posting.setDeliveryFee(
                Integer.parseInt(binding.deliveryFeeTextField.getEditText().getText().toString().trim()));
        posting.setContents(binding.contentsTextField.getEditText().getText().toString().trim());
        posting.setEtc(binding.etcTextField.getEditText().getText().toString().trim());

        viewModel.posting(posting);
        showToastMessage("포스팅이 완료되었습니다.");

        onBackPressed();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        binding.postingButton.setEnabled(getTextFieldValidation());
    }
}