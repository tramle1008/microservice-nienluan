// src/components/pagination/AdminOrderPagination.jsx
import * as React from 'react';
import Pagination from '@mui/material/Pagination';
import Stack from '@mui/material/Stack';
import { useSearchParams, useNavigate } from 'react-router-dom';

export default function AdminOrderPagination({ totalPages }) {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    // Backend dùng page=0 → URL sẽ có page=0,1,2...
    // Nhưng MUI Pagination hiển thị từ 1,2,3...
    const urlPage = parseInt(searchParams.get("page") ?? "0", 10); // backend page (0-based)
    const displayPage = urlPage + 1; // hiển thị cho người dùng (1-based)

    const handlePageChange = (event, value) => {
        const newBackendPage = value - 1; // chuyển từ 1-based → 0-based

        const newParams = new URLSearchParams(searchParams);
        newParams.set("page", newBackendPage.toString());

        // Nếu đang ở trang đầu tiên (page=0), có thể bỏ param page nếu muốn URL sạch hơn
        if (newBackendPage === 0) {
            newParams.delete("page");
        }

        navigate(`?${newParams.toString()}`);
    };

    // Nếu totalPages = 0 hoặc 1 → không hiện pagination
    if (!totalPages || totalPages <= 1) return null;

    return (
        <div className="flex justify-center py-10">
            <Stack spacing={2}>
                <Pagination
                    count={totalPages}
                    page={displayPage}
                    onChange={handlePageChange}
                    variant="outlined"
                    shape="rounded"
                    color="success"
                    size="large"
                    siblingCount={1}
                    boundaryCount={1}
                />
            </Stack>
        </div>
    );
}