import { useState } from "react";
import SearchByImageResults from "./SearchByImageResults";


const SearchByImagePage = () => {
    const [file, setFile] = useState(null);
    const [results, setResults] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
        setError("");
    };

    const handleSearch = async () => {
        if (!file) {
            setError("Vui lòng chọn một hình ảnh");
            return;
        }

        setLoading(true);
        setResults(null);

        const formData = new FormData();
        formData.append("image", file);

        try {
            const response = await fetch("http://localhost:8080/api/products/public/search-by-image", {
                method: "POST",
                body: formData,
            });

            if (!response.ok) throw new Error("Lỗi khi tìm kiếm");

            const data = await response.json();
            setResults(data);
        } catch (err) {
            setError("Không thể kết nối đến server hoặc lỗi xử lý hình ảnh");
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="max-w-4xl mx-auto py-12 px-4">
            <h1 className="text-2xl font-bold text-center mb-10 text-gray-800">
                Tìm kiếm sản phẩm bằng hình ảnh
            </h1>

            <div className="bg-white rounded-xl shadow-xl p-8">
                <div className="flex flex-col items-center gap-6">
                    <input
                        type="file"
                        accept="image/*"
                        onChange={handleFileChange}
                        className="block w-full text-sm text-gray-700 file:mr-4 file:py-3 file:px-6 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-emerald-700 file:text-white hover:file:bg-emerald-800"
                    />

                    {file && (
                        <div className="mt-4">
                            <img
                                src={URL.createObjectURL(file)}
                                alt="Preview"
                                className="max-h-64 rounded-lg shadow-md"
                            />
                        </div>
                    )}

                    <button
                        onClick={handleSearch}
                        disabled={loading}
                        className="bg-[#3a6fca] text-white py-3 px-4 rounded-lg font-semibold text-lg hover:bg-[#1b0b59] disabled:opacity-60"
                    >
                        {loading ? "Đang tìm kiếm..." : "Tìm kiếm"}
                    </button>

                    {error && <p className="text-red-600 text-center">{error}</p>}
                </div>
            </div>

            {/* Hiển thị kết quả - tái sử dụng hoàn toàn ProductCard */}
            {results && (
                <SearchByImageResults
                    results={results}
                    onClose={() => {
                        setResults(null);
                        setFile(null);
                    }}
                />
            )}
        </div>
    );
};

export default SearchByImagePage;