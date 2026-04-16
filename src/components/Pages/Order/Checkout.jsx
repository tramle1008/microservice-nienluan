import React, { useState } from 'react';
import {
    Stepper, Step, StepLabel, Box
} from '@mui/material';
import ShippingAddress from './ShippingAddress';
import SelectMethod from './SelectMethod';
import Payment from './Payment';
import Complete from './Complete';

const steps = ['Chọn địa chỉ giao hàng', 'Chọn phương thức thanh toán', 'Thanh toán', 'Hoàn tất'];

const Checkout = () => {
    const [activeStep, setActiveStep] = useState(0);
    const [addressId, setAddressId] = useState(null);
    const [paymentMethod, setPaymentMethod] = useState(null);

    const handleNext = () => setActiveStep((prev) => prev + 1);
    const handleBack = () => setActiveStep((prev) => prev - 1);
    const handleReset = () => setActiveStep(0);

    const renderStepContent = (step) => {
        switch (step) {
            case 0:
                return <ShippingAddress onNext={handleNext} setAddressId={setAddressId} />;
            case 1:
                return (
                    <SelectMethod
                        onNext={handleNext}
                        onBack={handleBack}
                        setPaymentMethod={setPaymentMethod}
                    />
                );
            case 2:
                return (
                    <Payment
                        onNext={handleNext}
                        onBack={handleBack}
                        addressId={addressId}
                        paymentMethod={paymentMethod}
                    />
                );
            case 3:
                return <Complete onReset={handleReset} />;
            default:
                return <div>Không tìm thấy bước này</div>;
        }
    };

    return (
        <div className='pt-5'>
            <Box sx={{ width: '100%' }}>
                <Stepper activeStep={activeStep} alternativeLabel>
                    {steps.map((label) => (
                        <Step key={label}>
                            <StepLabel>{label}</StepLabel>
                        </Step>
                    ))}
                </Stepper>

                <Box sx={{ mt: 4 }}>
                    {renderStepContent(activeStep)}
                </Box>
            </Box>
        </div>
    );
};

export default Checkout;
