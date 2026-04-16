import { useEffect, useState } from 'react';
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
} from 'recharts';
import api from '../../../api/api';

function DailyOrderChart() {
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);

    // State cho 2 input ngày
    const [fromDate, setFromDate] = useState('');
    const [toDate, setToDate] = useState('');

    // Hàm lấy tuần hiện tại (Chủ Nhật → Thứ Bảy)
    const getCurrentWeekRange = () => {
        const today = new Date();
        const start = new Date(today);
        start.setDate(today.getDate() - today.getDay()); // Chủ Nhật

        const end = new Date(start);
        end.setDate(start.getDate() + 6); // Thứ Bảy

        return {
            from: start.toISOString().split('T')[0],
            to: end.toISOString().split('T')[0],
        };
    };

    // Hàm fetch dữ liệu từ API
    const fetchOrderData = async (from, to) => {
        try {
            setLoading(true);
            const token = JSON.parse(localStorage.getItem("auth"))?.jwtToken;

            if (!token) {
                console.error("Không tìm thấy token!");
                return;
            }

            const response = await api.get('/orders/daily-count', {
                params: { from, to },
                headers: { Authorization: `Bearer ${token}` },
            });

            const apiData = response.data;

            // Tạo mảng đầy đủ các ngày trong khoảng from → to
            const startDate = new Date(from);
            const endDate = new Date(to);
            const fullData = [];

            for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
                const dateStr = d.toISOString().split('T')[0];
                const existing = apiData.find(item => item.date === dateStr);
                fullData.push({
                    date: dateStr,
                    count: existing ? existing.count : 0,
                });
            }

            setData(fullData);
        } catch (error) {
            console.error('Lỗi tải dữ liệu đơn hàng:', error);
            if (error.response?.status === 401) {
                alert('Phiên đăng nhập hết hạn!');
            }
        } finally {
            setLoading(false);
        }
    };

    // Load lần đầu: hiển thị tuần hiện tại
    useEffect(() => {
        const week = getCurrentWeekRange();
        setFromDate(week.from);
        setToDate(week.to);
        fetchOrderData(week.from, week.to);
    }, []);

    // Khi người dùng bấm "Xem" hoặc thay đổi ngày
    const handleView = () => {
        if (!fromDate || !toDate) {
            alert("Vui lòng chọn đầy đủ Từ ngày và Đến ngày!");
            return;
        }
        if (fromDate > toDate) {
            alert("Từ ngày không được lớn hơn Đến ngày!");
            return;
        }
        fetchOrderData(fromDate, toDate);
    };

    // Định dạng ngày tiếng Việt
    const formatVietnameseDate = (dateStr) => {
        const date = new Date(dateStr);
        const days = ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'];
        return `${days[date.getDay()]} ${date.getDate()}/${date.getMonth() + 1}`;
    };

    return (
        <div className="bg-white rounded-lg shadow-lg p-6">
            <h3 className="text-xl font-bold text-gray-800 mb-6 text-center">
                Số đơn hàng mới theo ngày
            </h3>

            {/* Ô chọn ngày */}
            <div className="flex flex-col sm:flex-row gap-4 mb-6 items-end">
                <div className="flex-1">
                    <label className="block text-sm font-medium text-gray-700 mb-1">Từ ngày</label>
                    <input
                        type="date"
                        value={fromDate}
                        onChange={(e) => setFromDate(e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />
                </div>

                <div className="flex-1">
                    <label className="block text-sm font-medium text-gray-700 mb-1">Đến ngày</label>
                    <input
                        type="date"
                        value={toDate}
                        onChange={(e) => setToDate(e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    />
                </div>

                <button
                    onClick={handleView}
                    className="px-6 py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition"
                >
                    Xem
                </button>
            </div>

            {/* Biểu đồ */}
            {loading ? (
                <div className="text-center py-10">
                    <div className="inline-block animate-spin rounded-full h-10 w-10 border-t-4 border-blue-600"></div>
                    <p className="mt-4 text-gray-600">Đang tải dữ liệu...</p>
                </div>
            ) : (
                <ResponsiveContainer width="100%" height={400}>
                    <LineChart data={data} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis
                            dataKey="date"
                            tickFormatter={formatVietnameseDate}
                            style={{ fontSize: '14px' }}
                        />
                        <YAxis allowDecimals={false} />
                        <Tooltip
                            labelFormatter={formatVietnameseDate}
                            formatter={(value) => [`${value} đơn hàng`, 'Số đơn']}
                            contentStyle={{ backgroundColor: '#fff', border: '1px solid #ccc', borderRadius: '8px' }}
                        />
                        <Line
                            type="monotone"
                            dataKey="count"
                            stroke="#3b82f6"
                            strokeWidth={3}
                            dot={{ fill: '#3b82f6', r: 6 }}
                            activeDot={{ r: 8 }}
                            name="Số đơn hàng"
                        />
                    </LineChart>
                </ResponsiveContainer>
            )}

            {/* Hiển thị khoảng thời gian đang xem */}
            <p className="text-center text-sm text-gray-500 mt-4">
                Đang hiển thị từ <strong>{formatVietnameseDate(fromDate)}</strong> đến <strong>{formatVietnameseDate(toDate)}</strong>
            </p>
        </div>
    );
}

export default DailyOrderChart;