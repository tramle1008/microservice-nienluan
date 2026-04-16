// src/store/reducers/store.js
import { configureStore } from '@reduxjs/toolkit';
import { productReducer } from './ProductReducer';

import { authReducer } from './authReducer';
import { addressReducer } from './addressReducer';
import { cartReducer } from './cartReducer';
import { orderReducer } from './orderReducer';
import { adminOrderReducer } from './adminOrderReducer';
import orderUserReducer from './orderUserReducer';
import { shippingFeeReducer } from './shippingFeeReducer';
import { discountSliderReducer } from './DiscountSliderReducer';
import categoriesProductReducer from './categoriesProductReducer';


const user = localStorage.getItem("auth")
    ? JSON.parse(localStorage.getItem("auth")) : [];


const initialState = {
    auth: { user: user },
};

const store = configureStore({
    reducer: {
        products: productReducer,
        // product: productReducer,
        auth: authReducer,
        address: addressReducer,
        cart: cartReducer,
        order: orderReducer,
        orderUser: orderUserReducer,
        shipping: shippingFeeReducer,
        adminOrders: adminOrderReducer,
        discountSlider: discountSliderReducer,
        categoryTree: categoriesProductReducer,
    },
    preloadedState: initialState,
});

export default store;

