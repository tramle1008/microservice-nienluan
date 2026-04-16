import React, { useState, useEffect } from 'react';
import axios from 'axios';
import toast from 'react-hot-toast';
import { useParams, useNavigate } from 'react-router-dom';
import AdminSidebar from './AdminSidebar';

function UpdateProductForm() {
    const { productId } = useParams();
    const navigate = useNavigate();

    const [loading, setLoading] = useState(true);
    const [categories, setCategories] = useState([]);
    const [rootList, setRootList] = useState([]);

    const [rootId, setRootId] = useState('');
    const [selectedLeaf, setSelectedLeaf] = useState('');

    const [productName, setProductName] = useState('');
    const [shortDesc, setShortDesc] = useState('');
    const [longDesc, setLongDesc] = useState('');
    const [price, setPrice] = useState('');

    const [mainImage, setMainImage] = useState(null);
    const [mainPreview, setMainPreview] = useState(''); // ảnh cũ hoặc mới

    const [variants, setVariants] = useState([]);

    const getToken = () => JSON.parse(localStorage.getItem("auth"))?.jwtToken;

    // Load danh mục + sản phẩm khi mở trang
    useEffect(() => {
        const fetchData = async () => {
            try {
                // 1. Load cây danh mục
                const catRes = await axios.get('http://localhost:8080/api/categories/public/tree');
                setCategories(catRes.data);
                setRootList(catRes.data);

                // 2. Load sản phẩm cần sửa
                const prodRes = await axios.get(`http://localhost:8080/api/products/public/${productId}`, {
                    headers: { Authorization: `Bearer ${getToken()}` }
                });

                const p = prodRes.data;
                setProductName(p.productName || '');
                setShortDesc(p.shortDescription || '');
                setLongDesc(p.longDescription || '');
                setPrice(p.price || '');

                // Xử lý ảnh chính
                setMainPreview(p.imageUrl || '');

                // Xử lý danh mục (tìm leaf category)
                const findLeaf = (nodes, targetId) => {
                    for (const node of nodes) {
                        if (node.categoryId === targetId) return node;
                        if (node.children) {
                            const found = findLeaf(node.children, targetId);
                            if (found) {
                                setRootId(node.categoryId);
                                return found;
                            }
                        }
                    }
                    return null;
                };

                // Giả sử bạn lưu categoryId trong product (hoặc variant)
                // Nếu không có thì bỏ qua phần này
                if (p.categoryId) {
                    const leaf = findLeaf(catRes.data, p.categoryId);
                    if (leaf) setSelectedLeaf(p.categoryId);
                }

                // Load variants
                if (p.variants && p.variants.length > 0) {
                    setVariants(p.variants.map(v => ({
                        variantId: v.variantId,
                        color: v.color || '',
                        stock: v.stockQuantity || 0,
                        priceOverride: v.priceOverride || '',
                        imageUrl: v.imageUrl || '',
                        preview: v.imageUrl || '', // hiển thị ảnh cũ
                        image: null // file mới (nếu người dùng đổi)
                    })));
                } else {
                    setVariants([{ color: '', stock: 0, priceOverride: '', image: null, preview: '' }]);
                }

            } catch (err) {
                toast.error('Lỗi tải dữ liệu: ' + (err.response?.data || err.message));
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [productId]);

    // Xử lý ảnh chính
    const handleMainImage = (e) => {
        const file = e.target.files[0];
        if (file) {
            setMainImage(file);
            setMainPreview(URL.createObjectURL(file));
        }
    };

    // Variant handlers
    const addVariant = () => {
        setVariants([...variants, {
            color: '', stock: 0, priceOverride: '', image: null, preview: '', variantId: null
        }]);
    };

    const removeVariant = (index) => {
        if (variants.length > 1) {
            setVariants(variants.filter((_, i) => i !== index));
        }
    };

    const updateVariant = (index, field, value) => {
        const newVariants = [...variants];
        newVariants[index][field] = value;
        setVariants(newVariants);
    };

    const handleVariantImage = (index, e) => {
        const file = e.target.files[0];
        if (file) {
            const newVariants = [...variants];
            newVariants[index].image = file;
            newVariants[index].preview = URL.createObjectURL(file);
            setVariants(newVariants);
        }
    };

    // Submit form
    const handleSubmit = async (e) => {
        e.preventDefault();

        const formData = new FormData();

        const updateDTO = {
            productName: productName || null,
            shortDescription: shortDesc || null,
            longDescription: longDesc || null,
            price: price ? Number(price) : null,
            variants: variants.map(v => ({
                variantId: v.variantId || null,
                color: v.color || null,
                stockQuantity: v.stock !== undefined ? Number(v.stock) : null,
                priceOverride: v.priceOverride ? Number(v.priceOverride) : null
            })).filter(v => v.variantId || v.color) // chỉ gửi variant có dữ liệu
        };

        formData.append('updateDTO', JSON.stringify(updateDTO));
        if (mainImage) formData.append('mainImage', mainImage);

        variants.forEach(v => {
            if (v.image) {
                formData.append('variantImages', v.image);
            }
        });

        try {
            const token = getToken();
            await axios.put(
                `http://localhost:8080/api/products/auth/${productId}`,
                formData,
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,

                    }
                }
            );

            toast.success('Cập nhật sản phẩm thành công!');
            navigate('/admin/product'); // quay lại danh sách hoặc ở lại tùy bạn
        } catch (err) {
            const msg = err.response?.data || err.message;
            toast.error('Lỗi cập nhật: ' + msg);
            console.error(err);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-screen">
                <div className="text-2xl font-bold text-gray-600">Đang tải sản phẩm...</div>
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto p-8 bg-white rounded-2xl shadow-xl mt-10">
            <AdminSidebar />
            <button
                onClick={() => navigate(-1)} // Quay lại trang trước (rất tiện!)
                className="flex items-center gap-2 px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg transition"
            >

                <span>Quay lại</span>
            </button>
            <h1 className="text-3xl font-bold text-center my-2 text-blue-700">
                CHỈNH SỬA SẢN PHẨM
            </h1>

            <form onSubmit={handleSubmit} className="space-y-8">
                {/* Thông tin cơ bản */}
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
                    <textarea value={longDesc} onChange={(e) => setLongDesc(e.target.value)} rows={6} className="w-full border-2 rounded-lg px-4 py-3"></textarea>
                </div>

                {/* Ảnh chính */}
                <div>
                    <label className="block font-semibold mb-2">Ảnh chính hiện tại</label>
                    {mainPreview && <img src={mainPreview} alt="Main" className="mt-4 max-h-80 rounded-lg shadow-md" />}
                    <input type="file" accept="image/*" onChange={handleMainImage} className="mt-4 block w-full" />
                    <p className="text-sm text-gray-500 mt-2">Để trống nếu không muốn thay đổi</p>
                </div>

                {/* Biến thể */}
                <div>
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-2xl font-bold">Biến thể sản phẩm</h3>
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
                                    placeholder="Ví dụ: Nâu, Trắng"
                                />
                            </div>

                            <div className="w-40">
                                <label className="block font-medium">Tồn kho</label>
                                <input
                                    type="number"
                                    value={variant.stock}
                                    onChange={(e) => updateVariant(index, 'stock', e.target.value)}
                                    className="mt-1 w-full border rounded px-3 py-2"
                                    min="0"
                                />
                            </div>

                            <div className="w-48">
                                <label className="block font-medium">Giá riêng</label>
                                <input
                                    type="number"
                                    value={variant.priceOverride}
                                    onChange={(e) => updateVariant(index, 'priceOverride', e.target.value)}
                                    className="mt-1 w-full border rounded px-3 py-2"
                                    placeholder="Để trống = dùng giá gốc"
                                />
                            </div>

                            <div>
                                <label className="block font-medium">Ảnh hiện tại</label>
                                {variant.preview && <img src={variant.preview} alt="variant" className="mt-2 max-h-32 rounded" />}
                                <input type="file" accept="image/*" onChange={(e) => handleVariantImage(index, e)} className="mt-2 block" />
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

                <div className="flex gap-4">
                    <button
                        type="submit"
                        className="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xl py-2 rounded-xl shadow-lg transition"
                    >
                        CẬP NHẬT SẢN PHẨM
                    </button>

                </div>
            </form>
        </div>
    );
}

export default UpdateProductForm;