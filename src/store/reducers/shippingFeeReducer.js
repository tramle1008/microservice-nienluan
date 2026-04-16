const initialState = {
    loadingA: false,
    fee: null,
    errorA: null,
};

export const shippingFeeReducer = (state = initialState, action) => {
    switch (action.type) {
        case "SHIPPING_FEE_REQUEST":
            return { ...state, loading: true, error: null };

        case "SHIPPING_FEE_SUCCESS":
            return { ...state, loading: false, fee: action.payload };

        case "SHIPPING_FEE_FAIL":
            return { ...state, loading: false, error: action.payload };

        default:
            return state;
    }
};
