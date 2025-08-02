import {combineReducers, configureStore} from '@reduxjs/toolkit';
import {FLUSH, PAUSE, PERSIST, persistReducer, persistStore, PURGE, REGISTER, REHYDRATE} from 'redux-persist';
import storage from 'redux-persist/lib/storage';
import authReducer, {logout} from './authSlice';
import periodReducer from './periodSlice';

const persistConfig = {
    key: 'root',
    storage,
    whitelist: ['auth', 'period']
};

const appReducer = combineReducers({
    auth: authReducer,
    period: periodReducer,
});

const rootReducer = (state, action) => {
    if (action.type === logout.type) {
        storage.removeItem('persist:root');
        return appReducer(undefined, action);
    }

    return appReducer(state, action);
};


const persistedReducer = persistReducer(persistConfig, rootReducer);

export const store = configureStore({
    reducer: persistedReducer,
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware({
            serializableCheck: {
                ignoredActions: [FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER],
            },
        }),
});

export const persistor = persistStore(store);