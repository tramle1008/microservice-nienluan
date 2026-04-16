const initialState = {
    products: [],
    pagination: {
        pageNumber: 0,
        pageSize: 10,
        totalElements: 0,
        totalPages: 1,
        lastPage: true,
    },
    isLoading: false,
    error: null,
    currentCategoryId: null, // để biết đang xem category nào (tùy chọn)
};

const categoriesProductReducer = (state = initialState, action) => {
    switch (action.type) {
        case "FETCH_CATEGORY_TREE_PRODUCTS_START":
            return {
                ...state,
                isLoading: true,
                error: null,
            };

        case "FETCH_CATEGORY_TREE_PRODUCTS_SUCCESS":
            return {
                ...state,
                isLoading: false,
                products: action.payload.products,
                pagination: action.payload.pagination,
                currentCategoryId: action.payload.categoryId || state.currentCategoryId,
            };

        case "FETCH_CATEGORY_TREE_PRODUCTS_ERROR":
            return {
                ...state,
                isLoading: false,
                error: action.payload,
            };

        // Optional: Reset khi rời trang
        case "CLEAR_CATEGORY_TREE_PRODUCTS":
            return initialState;

        default:
            return state;
    }
};

export default categoriesProductReducer;