package com.example.foodsaver.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodsaver.data.model.User;
import com.example.foodsaver.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private AuthRepository repository;

    // MutableLiveData allows us to update the value from the background thread
    private MutableLiveData<User> authenticatedUser = new MutableLiveData<>();
    private MutableLiveData<String> authError = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    // The UI will observe these LiveData objects
    public LiveData<User> getAuthenticatedUser() {
        return authenticatedUser;
    }

    public LiveData<String> getAuthError() {
        return authError;
    }

    public void login(String email, String password) {
        repository.loginUser(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                // postValue is required here because this is triggered from a background thread
                authenticatedUser.postValue(user);
            }

            @Override
            public void onFailure(String error) {
                authError.postValue(error);
            }
        });
    }

    public void register(User user) {
        repository.registerUser(user, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                authenticatedUser.postValue(user);
            }

            @Override
            public void onFailure(String error) {
                authError.postValue(error);
            }
        });
    }
}