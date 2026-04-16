import React, { useState, useEffect } from 'react';
import axios from 'axios';
import toast from 'react-hot-toast';
import AdminSidebar from './AdminSidebar';

function AddProductForm() {
    const [categories, setCategories] = useState([]);
    const [rootId, setRootId] = useState('');


    const [rootList, setRootList] = useState([]);
    const [selectedLeaf, setSelectedLeaf] = useState('');

    const [productName, setProductName] = useState('');
    const [shortDesc, setShortDesc] = useState('');
    const [longDesc, setLongDesc] = useState('');
    const [price, setPrice] = useState('');

    const [mainImage, setMainImage] = useState(null);
    const [mainPreview, setMainPreview] = useState('');

    const [variants, setVariants] = useState([
        { color: '', stock: 0, priceOverride: '', image: null, preview: '' }
    ]);

    // Load cây danh mục khi mở trang
    useEffect(() => {
        axios.get('http://localhost:8080/api/categories/public/tree')
            .then(res => {
                setCategories(res.data);
                setRootList(res.data);
            })
            .catch(err => alert('Lỗi tải danh mục: ' + err.message));
    }, []);


    // Xử lý ảnh chính
    const handleMainImage = (e) => {
        const file = e.target.files[0];
        if (file) {
            setMainImage(file);
            setMainPreview(URL.createObjectURL(file));
        }
    };

    // Thêm biến thể
    const addVariant = () => {
        setVariants([...variants, { color: '', stock: 0, priceOverride: '', image: null, preview: '' }]);
    };

    // Xóa biến thể
    const removeVariant = (index) => {
        if (variants.length > 1) {
            setVariants(variants.filter((_, i) => i !== index));
        }
    };

    // Cập nhật variant
    const updateVariant = (index, field, value) => {
        const newVariants = [...variants];
        newVariants[index][field] = value;
        setVariants(newVariants);
    };

    // Xử lý ảnh variant
    const handleVariantImage = (index, e) => {
        const file = e.target.files[0];
        if (file) {
            const newVariants = [...variants];
            newVariants[index].image = file;
            newVariants[index].preview = URL.createObjectURL(file);
            setVariants(newVariants);
        }
    };
    const getToken = () => JSON.parse(localStorage.getItem("auth"))?.jwtToken;

    // Submit form
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!selectedLeaf) {
            toast.error('Vui lòng chọn loại sản phẩm!');
            return;
        }

        const formData = new FormData();
        formData.append('categoryId', selectedLeaf); // ← Đây chính là categoryId cuối cùng (Bàn ăn, Sofa, Giường ngủ...)
        const createDTO = {
            productName: productName,
            shortDescription: shortDesc,
            longDescription: longDesc,
            price: Number(price),
            variants: variants.map(v => ({
                color: v.color,
                stockQuantity: Number(v.stock) || 0,
                priceOverride: v.priceOverride ? Number(v.priceOverride) : null
            }))
        };

        formData.append('createDTO', JSON.stringify(createDTO));
        if (mainImage) formData.append('mainImage', mainImage);

        // Thêm ảnh variant theo đúng thứ tự
        variants.forEach(v => {
            if (v.image) {
                formData.append('variantImages', v.image);
            }
        });

        try {
            const token = getToken();
            const res = await axios.post(
                'http://localhost:8080/api/products/auth/with-variants',
                formData,
                {
                    headers: {
                        'Authorization': token ? `Bearer ${token}` : '',
                    }
                }
            );

            toast.success('Thêm sản phẩm thành công!');
            console.log(res.data);
            // Reset form
            setProductName(''); setShortDesc(''); setLongDesc(''); setPrice('');
            setMainImage(null); setMainPreview('');
            setVariants([{ color: '', stock: 0, priceOverride: '', image: null, preview: '' }]);
            setRootId(''); setSelectedLeaf('');
        } catch (err) {
            const msg = err.response?.data || err.message;
            toast.error('Lỗi: ' + msg);
            console.error(err);
        }
    };

    return (
        <div className="max-w-2xl mx-auto p-8 bg-white rounded-2xl shadow-xl mt-10">
            <AdminSidebar />
            <h1 className="text-3xl font-bold text-center my-10 rounded text-blue-700">THÊM SẢN PHẨM MỚI</h1>

            <form onSubmit={handleSubmit} className="space-y-8">

                <div className="grid grid-cols-2 gap-8 mb-10">
                    <div>
                        <label className="block font-bold text-xl mb-3 text-gray-700">Khu vực</label>
                        <select
                            value={rootId}
                            onChange={(e) => {
                                setRootId(e.target.value);
                                setSelectedLeaf(''); // reset khi đổi khu vực
                            }}
                            className="w-full border-2 border-gray-300 rounded-xl px-5 py-4 text-lg"
                            required
                        >
                            <option value="">-- Chọn khu vực --</option>
                            {rootList.map(root => (
                                <option key={root.categoryId} value={root.categoryId}>
                                    {root.categoryName}
                                </option>
                            ))}
                        </select>
                    </div>

                    {/* 2. Chọn loại sản phẩm (Leaf - nơi thêm hàng) */}
                    <div>
                        <label className="block font-semibold mb-2 text-lg text-red-600">
                            Loại sản phẩm (nơi đặt hàng) *
                        </label>
                        <select
                            value={selectedLeaf}
                            onChange={(e) => setSelectedLeaf(e.target.value)}
                            className="w-full border-2 border-red-300 rounded-lg px-4 py-3 text-lg"
                            disabled={!rootId}
                            required
                        >
                            <option value="">-- Chọn loại sản phẩm --</option>
                            {rootId && categories
                                .find(c => c.categoryId === Number(rootId))
                                ?.children
                                ?.map(child => (
                                    <option key={child.categoryId} value={child.categoryId}>
                                        {child.categoryName}
                                    </option>
                                ))
                            }
                        </select>
                    </div>
                </div>
                {/* Thông tin sản phẩm */}
                <div className="grid grid-cols-2 gap-8">
                    <div>
                        <label className="block font-semibold mb-2">Tên sản phẩm *</label>
                        <input type="text" value={productName} onChange={(e) => setProductName(e.target.value)} className="w-full border-2 rounded-lg px-4 py-3" required />
                    </div>
                    <div>
                        <label className="block font-semibold mb-2">Giá gốc (VNĐ) *</label>
                        <input type="number" value={price} onChange={(e) => setPrice(e.target.value)} className="w-full border-2 rounded-lg px-4 py-3" required />
                    </div>
                </div>

                <div>
                    <label className="block font-semibold mb-2">Mô tả ngắn</label>
                    <input type="text" value={shortDesc} onChange={(e) => setShortDesc(e.target.value)} className="w-full border-2 rounded-lg px-4 py-3" />
                </div>

                <div>
                    <label className="block font-semibold mb-2">Mô tả chi tiết</label>
                    <textarea value={longDesc} onChange={(e) => setLongDesc(e.target.value)} rows={5} className="w-full border-2 rounded-lg px-4 py-3"></textarea>
                </div>

                {/* Ảnh chính */}
                <div>
                    <label className="block font-semibold mb-2">Ảnh chính sản phẩm *</label>
                    <input type="file" accept="image/*" onChange={handleMainImage} required className="w-full" />
                    {mainPreview && <img src={mainPreview} alt="Preview" className="mt-4 max-h-80 rounded-lg shadow-md" />}
                </div>

                {/* Danh sách biến thể */}
                <div>
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-2xl font-bold">Biến thể sản phẩm (màu sắc)</h3>
                        <button type="button" onClick={addVariant} className="bg-gray-600 text-white px-6 py-2 rounded-lg hover:bg-gray-700 font-semibold">
                            + Thêm biến thể
                        </button>
                    </div>

                    {variants.map((variant, index) => (
                        <div key={index} className="bg-gray-50 p-6 rounded-xl mb-4 border-2 border-gray-200 flex gap-4 items-end flex-wrap">
                            <div className="flex-1 min-w-64">
                                <label className="block font-medium">Màu sắc</label>
                                <input
                                    type="text"
                                    value={variant.color}
                                    onChange={(e) => updateVariant(index, 'color', e.target.value)}
                                    className="mt-1 w-full border rounded px-3 py-2"
                                    placeholder="Ví dụ: Nâu, Xám"
                                    required
                                />
                            </div>

                            <div className="w-40">
                                <label className="block font-medium">Số lượng tồn</label>
                                <input
                                    type="number"
                                    value={variant.stock}
                                    onChange={(e) => updateVariant(index, 'stock', e.target.value)}
                                    className="mt-1 w-full border rounded px-3 py-2"
                                    min="0"
                                    required
                                />
                            </div>


                            <div className="w-48">
                                <label className="block font-medium">Giá riêng (nếu có)</label>
                                <input
                                    type="number"
                                    value={variant.priceOverride}
                                    onChange={(e) => updateVariant(index, 'priceOverride', e.target.value)}
                                    className="mt-1 w-full border rounded px-3 py-2"
                                    placeholder="Để trống = dùng giá gốc"
                                />
                            </div>

                            <div>
                                <label className="block font-medium">Ảnh biến thể</label>
                                <input type="file" accept="image/*" onChange={(e) => handleVariantImage(index, e)} className="mt-1" />
                                {variant.preview && <img src={variant.preview} alt="preview" className="mt-2 max-h-32 rounded" />}
                            </div>

                            <button
                                type="button"
                                onClick={() => removeVariant(index)}
                                className={`ml-4 text-white px-4 py-2 rounded font-bold ${variants.length === 1 ? 'bg-gray-400' : 'bg-red-600 hover:bg-red-700'}`}
                                disabled={variants.length === 1}
                            >
                                XÓA
                            </button>
                        </div>
                    ))}
                </div>

                <button
                    type="submit"
                    className="w-full bg-green-600 hover:bg-green-700 text-white font-bold text-md py-3 rounded-xl shadow-lg transition"
                >
                    THÊM SẢN PHẨM NGAY
                </button>
            </form >
        </div >
    );
}

export default AddProductForm;