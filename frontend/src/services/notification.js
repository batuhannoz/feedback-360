import { toast } from 'sonner';

export const showErrorToast = (message) => {
    toast.error(message);
};

export const showSuccessToast = (message) => {
    toast.success(message);
};