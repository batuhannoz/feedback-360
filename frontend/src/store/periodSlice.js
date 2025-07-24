import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import EvaluationPeriodService from '../services/evaluationPeriodService';

export const fetchPeriods = createAsyncThunk('periods/fetchPeriods', async () => {
    const response = await EvaluationPeriodService.getPeriods();
    return response.data;
});

const periodSlice = createSlice({
    name: 'period',
    initialState: {
        periods: [],
        selectedPeriod: null,
        status: 'idle',
        error: null
    },
    reducers: {
        setSelectedPeriod: (state, action) => {
            state.selectedPeriod = action.payload;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchPeriods.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(fetchPeriods.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.periods = action.payload;
                if (!state.selectedPeriod && action.payload.length > 0) {
                    state.selectedPeriod = action.payload[0];
                }
            })
            .addCase(fetchPeriods.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.error.message;
            });
    }
});

export const { setSelectedPeriod } = periodSlice.actions;

export default periodSlice.reducer;
