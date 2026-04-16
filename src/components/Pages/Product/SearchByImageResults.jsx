import { useState } from "react";

import { FaTimes } from "react-icons/fa";
import ProductCard from "./ProductCard";

const SearchByImageResults = ({ results, onClose }) => {
    const [loading, setLoading] = useState(false);

    if (!results || results.length === 0) {
        return (
            <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
                <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full p-8 relative">
                    <button
                        onClick={onClose}
                        className="absolute top-4 right-4 text-gray-500 hover:text-gray-800"
                    >
                        <FaTimes size={24} />
                    </button>
                    <h2 className="text-2xl font-bold text-center mb-6">
                        Kết quả tìm kiếm theo hình ảnh
                    </h2>
                    <p className="text-center text-gray-600">
                        Không tìm thấy sản phẩm tương tự.
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 overflow-y-auto">
            <div className="min-h-screen py-8 px-4">
                <div className="max-w-7xl mx-auto">
                    {/* Header */}
                    <div className="bg-white rounded-t-xl shadow-lg p-6 relative">
                        <button
                            onClick={onClose}
                            className="absolute top-4 right-6 text-gray-500 hover:text-gray-800"
                        >
                            <FaTimes size={28} />
                        </button>
                        <h2 className="text-3xl font-bold text-center text-emerald-800">
                            Kết quả tìm kiếm theo hình ảnh
                        </h2>
                        <p className="text-center text-gray-600 mt-2">
                            Tìm thấy {results.length} sản phẩm tương tự
                        </p>
                    </div>

                    {/* Danh sách sản phẩm */}
                    <div className="bg-gray-50 rounded-b-xl shadow-lg p-6">
                        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                            {results.map((product) => (
                                <ProductCard key={product.productId} product={product} />
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SearchByImageResults;