import React, { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import AuthService from '../services/authService';

const EmployeeInvitePage = () => {
    const { token } = useParams();
    const [formData, setFormData] = useState({
        password: '',
        passwordAgain: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (formData.parola !== formData.parolaTekrar) {
            setError('Parolalar eşleşmiyor.');
            return;
        }
        setError('');
        setLoading(true);
        try {
            await AuthService.completeEmployeeInvitation({
                invitationToken: token,
                password: formData.password,
            });
            alert('Kaydınız başarıyla tamamlandı! Şimdi giriş yapabilirsiniz.');
            navigate('/sign-in');
        } catch (err) {
            setError(err.response?.data?.message || 'Davet geçersiz veya süresi dolmuş olabilir.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-gray-100 px-4 sm:px-6 lg:px-8">
            <div className="w-full max-w-md space-y-8">
                <div>
                    <h2 className="mt-6 text-center text-3xl font-bold tracking-tight text-gray-900">
                        Kaydınızı Tamamlayın
                    </h2>
                    <p className="mt-2 text-center text-sm text-gray-600">
                        Sisteme hoş geldiniz! Hesabınız için bir parola belirleyin.
                    </p>
                </div>
                <form className="mt-8 space-y-6 rounded-xl bg-white p-8 shadow-lg" onSubmit={handleSubmit}>
                    <div className="space-y-4">
                        <div>
                            <label htmlFor="parola" className="sr-only">Yeni Parola</label>
                            <input id="password" name="password" type="password" value={formData.password} onChange={handleChange} required minLength="6" className="relative block w-full appearance-none rounded-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-500 focus:z-10 focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm" placeholder="Yeni Parola"/>
                        </div>
                        <div>
                            <label htmlFor="parolaTekrar" className="sr-only">Parola Tekrar</label>
                            <input id="passwordAgain" name="passwordAgain" type="password" value={formData.passwordAgain} onChange={handleChange} required className="relative block w-full appearance-none rounded-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-500 focus:z-10 focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm" placeholder="Parola Tekrar"/>
                        </div>
                    </div>

                    {error && (
                        <div className="rounded-md bg-red-50 p-4">
                            <p className="text-sm text-red-700">{error}</p>
                        </div>
                    )}

                    <div>
                        <button type="submit" disabled={loading} className="group relative flex w-full justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:bg-indigo-400 disabled:cursor-not-allowed">
                            {loading ? 'Hesap Oluşturuluyor...' : 'Hesabı Oluştur'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default EmployeeInvitePage;