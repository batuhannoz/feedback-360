import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import AuthService from '../services/authService';
import {useDispatch} from "react-redux";
import { loginSuccess } from '../store/authSlice';

const SignInPage = () => {
    const [formData, setFormData] = useState({ email: '', password: '' });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const location = useLocation();

    useEffect(() => {
        if (location.state?.email) {
            setFormData(prev => ({ ...prev, email: location.state.email }));
        }
    }, [location.state]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const response = await AuthService.signIn({
                email: formData.email,
                password: formData.password,
            });

            dispatch(loginSuccess({
                accessToken: response.data.accessToken,
                refreshToken: response.data.refreshToken,
            }));

            navigate('/');

        } catch (err) {
            setError(err.response?.data?.message || 'E-posta veya parola hatalı.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-gray-100 px-4 sm:px-6 lg:px-8">
            <div className="w-full max-w-md space-y-8">
                <div>
                    <h2 className="mt-6 text-center text-3xl font-bold tracking-tight text-gray-900">
                        Hesabınıza Giriş Yapın
                    </h2>
                    <p className="mt-2 text-center text-sm text-gray-600">
                        veya{' '}
                        <Link to="/company-sign-up" className="font-medium text-indigo-600 hover:text-indigo-500">
                            yeni bir firma kaydı oluşturun
                        </Link>
                    </p>
                </div>
                <form className="mt-8 space-y-6 rounded-xl bg-white p-8 shadow-lg" onSubmit={handleSubmit}>
                    <div className="space-y-4 rounded-md shadow-sm">
                        <div>
                            <label htmlFor="email" className="sr-only">E-posta Adresi</label>
                            <input id="email" name="email" type="email" value={formData.email} onChange={handleChange} required className="relative block w-full appearance-none rounded-t-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-500 focus:z-10 focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm" placeholder="E-posta Adresi"/>
                        </div>
                        <div>
                            <label htmlFor="password" className="sr-only">Parola</label>
                            <input id="password" name="password" type="password" value={formData.password} onChange={handleChange} required className="relative block w-full appearance-none rounded-b-md border border-gray-300 px-3 py-2 text-gray-900 placeholder-gray-500 focus:z-10 focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm" placeholder="Parola"/>
                        </div>
                    </div>

                    {error && (
                        <div className="rounded-md bg-red-50 p-4">
                            <p className="text-sm text-red-700">{error}</p>
                        </div>
                    )}

                    <div>
                        <button type="submit" disabled={loading} className="group relative flex w-full justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:bg-indigo-400 disabled:cursor-not-allowed">
                            {loading ? 'Giriş Yapılıyor...' : 'Giriş Yap'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default SignInPage;