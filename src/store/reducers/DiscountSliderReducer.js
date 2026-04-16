const initialState = {
    products: [],
    loading: false,
    error: null,
};

export const discountSliderReducer = (state = initialState, action) => {
    switch (action.type) {
        case "DISCOUNTED_PRODUCTS_REQUEST":
            return { ...state, loading: true, error: null };

        case "DISCOUNTED_PRODUCTS_SUCCESS":
            return { ...state, loading: false, products: action.payload };

        case "DISCOUNTED_PRODUCTS_FAIL":
            return { ...state, loading: false, error: action.payload };

        default:
            return state;
    }
};