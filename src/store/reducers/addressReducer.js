const initialState = {
    addresses: [],
    loading: false,
    error: null,
};

export const addressReducer = (state = initialState, action) => {
    switch (action.type) {
        case "ADDRESS_FETCH_REQUEST":
            return { ...state, loading: true, error: null };
        case "ADDRESS_FETCH_SUCCESS":
            return { ...state, loading: false, addresses: action.payload };
        case "ADDRESS_FETCH_FAIL":
            return { ...state, loading: false, error: action.payload };
        default:
            return state;
    }
};
