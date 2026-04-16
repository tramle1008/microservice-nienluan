import * as React from 'react';
import Pagination from '@mui/material/Pagination';
import Stack from '@mui/material/Stack';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';

export default function PaginationRounded({ numberofPage }) {
    const [searchParams] = useSearchParams();
    const location = useLocation();
    const currentPath = location.pathname;
    const navigate = useNavigate();

    const param = new URLSearchParams(searchParams);
    const currentPage = Number((searchParams.get("page")) || 1);

    const onChangeHandler = (e, value) => {
        param.set("page", value.toString());
        const url = `${currentPath}?${param.toString()}`;

        console.log(" Điều hướng đến:", url);
        navigate(url);
    };

    return (
        <div className="flex justify-center p-10 ">

            <Stack spacing={2} className="py-5 flex justify-center">
                <Pagination
                    count={numberofPage}
                    page={currentPage}
                    variant="outlined"
                    shape="rounded"
                    color="success"
                    onChange={onChangeHandler}
                />
            </Stack>
        </div>
    );
}
