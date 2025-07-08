import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import AuthService from '../services/authService';

const CompanySignUpPage = () => {
    const [formData, setFormData] = useState({
        companyName: '',
        email: '',
        password: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await AuthService.companySignUp(formData);
            alert('Firma kaydınız başarıyla oluşturuldu! Giriş sayfasına yönlendiriliyorsunuz.');
            navigate('/sign-in');
        } catch (err) {
            setError(err.response?.data?.message || 'Bir hata oluştu. Lütfen bilgileri kontrol edip tekrar deneyin.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-gray-100 px-4 sm:px-6 lg:px-8">
            <div className="w-full max-w-md space-y-8">
                <div>
                    <h2 className="mt-6 text-center text-3xl font-bold tracking-tight text-gray-900">
                        Yeni Firma Kaydı
                    </h2>
                    <p className="mt-2 text-center text-sm text-gray-600">
                        Şirketinizi ve ilk yönetici hesabını oluşturun.
                    </p>
                </div>
                <form className="mt-8 space-y-6 rounded-xl bg-white p-8 shadow-lg" onSubmit={handleSubmit}>
                    <div className="space-y-4">
                        <div>
                            <label htmlFor="firmaAdi" className="sr-only">Firma Adı</label>
                            <input id="companyName" name="companyName" type="text" value={formData.companyName} onChange={handleChange} required className="relative block w-full appearance-none rounded-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-500 focus:z-10 focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm" placeholder="Firma Adı"/>
                        </div>
                        <div>
                            <label htmlFor="email" className="sr-only">E-posta Adresiniz (Yönetici)</label>
                            <input id="email" name="email" type="email" value={formData.email} onChange={handleChange} required className="relative block w-full appearance-none rounded-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-500 focus:z-10 focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm" placeholder="E-posta Adresiniz (Yönetici)"/>
                        </div>
                        <div>
                            <label htmlFor="parola" className="sr-only">Parola</label>
                            <input id="password" name="password" type="password" value={formData.password} onChange={handleChange} required minLength="6" className="relative block w-full appearance-none rounded-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-500 focus:z-10 focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm" placeholder="Parola"/>
                        </div>
                    </div>

                    {error && (
                        <div className="rounded-md bg-red-50 p-4">
                            <p className="text-sm text-red-700">{error}</p>
                        </div>
                    )}

                    <div>
                        <button type="submit" disabled={loading} className="group relative flex w-full justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:bg-indigo-400 disabled:cursor-not-allowed">
                            {loading ? 'Kaydediliyor...' : 'Kaydı Tamamla'}
                        </button>
                    </div>
                    <p className="text-center text-sm text-gray-600">
                        Zaten bir hesabınız var mı?{' '}
                        <Link to="/sign-in" className="font-medium text-indigo-600 hover:text-indigo-500">
                            Giriş Yapın
                        </Link>
                    </p>
                </form>
            </div>
        </div>
    );
};

export default CompanySignUpPage;