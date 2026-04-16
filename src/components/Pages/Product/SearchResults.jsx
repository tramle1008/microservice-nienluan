import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { fetchCategories, searchProducts } from "../../../store/actions";
import ProductCard from "./ProductCard";
import PaginationRounded from "../../PaginationRounded";
import ReusableFilter from "../../ReusableFilter";
import useProductFilter from "../../useProductFilter";
import { useDispatch, useSelector } from "react-redux";

const SearchResults = () => {
    const dispatch = useDispatch();
    const location = useLocation();
    const queryParams = new URLSearchParams(location.search);
    const keyword = queryParams.get("keyword") || "";

    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const { pagination } = useSelector((state) => state.products);
    const categories = useSelector((state) => state.products.categories) || [];

    useEffect(() => {
        dispatch(fetchCategories());
    }, [dispatch]);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            const data = await searchProducts(keyword);
            if (data) setProducts(data.content || []);
            setLoading(false);
        };
        fetchData();
    }, [keyword]);

    if (loading) {
        return (
            <div className="min-h-screen bg-[#f1f7f0] flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-b-4 border-emerald-700 mx-auto"></div>
                    <p className="mt-6 text-xl text-gray-700">Đang tìm kiếm sản phẩm...</p>
                </div>
            </div>
        );
    }

    if (!products.length) {
        return (
            <div className="min-h-screen bg-[#f1f7f0] flex flex-col items-center justify-center py-20">
                <div className="text-center max-w-md">
                    <div className="text-6xl mb-6">Không tìm thấy</div>
                    <p className="text-2xl text-gray-600 mb-4">
                        Không có sản phẩm nào cho từ khóa
                    </p>
                    <p className="text-3xl font-bold text-emerald-700 bg-emerald-50 px-6 py-3 rounded-2xl inline-block">
                        "{keyword}"
                    </p>
                    <p className="mt-8 text-lg text-gray-500">
                        Hãy thử tìm với từ khóa khác nhé!
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#f1f7f0]">


            {/* Danh sách sản phẩm */}
            <div className="max-w-7xl mx-auto px-6 py-12">
                <div className="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
                    {products.map((product) => (
                        <div
                            key={product.productId}
                            className="transform transition-all duration-300 hover:scale-105 hover:shadow-2xl"
                        >
                            <ProductCard product={product} />
                        </div>
                    ))}
                </div>

                {/* Phân trang */}
                {pagination?.totalPages > 1 && (
                    <div className="flex justify-center mt-16">
                        <PaginationRounded
                            numberofPage={pagination?.totalPages}
                            totalProducts={pagination?.totalElements}
                        />
                    </div>
                )}
            </div>
        </div>
    );
};

export default SearchResults;