
const initialState = {
    orders: [],
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
    lastPage: false,
    loading: false,
    error: null,
};

export const adminOrderReducer = (state = initialState, action) => {
    switch (action.type) {
        case "ORDER_FETCH_REQUEST":
            return { ...state, loading: true, error: null };
        case "ORDER_FETCH_SUCCESS":
            return {
                ...state,
                loading: false,
                orders: action.payload.orders,
                pageNumber: action.payload.pageNumber,
                pageSize: action.payload.pageSize,
                totalElements: action.payload.totalElements,
                totalPages: action.payload.totalPages,
                lastPage: action.payload.lastPage,
            };
        case "ORDER_FETCH_FAIL":
            return { ...state, loading: false, error: action.payload };
        default:
            return state;
    }
};
