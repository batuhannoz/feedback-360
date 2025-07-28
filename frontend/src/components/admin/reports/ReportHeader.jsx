import React from 'react';
import { Card, CardHeader, CardTitle, CardDescription } from '../../../components/ui/card';

const ReportHeader = ({ user, period }) => {
    return (
        <Card className="mb-6">
            <CardHeader>
                <CardTitle className="text-2xl">{`${user.firstName} ${user.lastName}`}</CardTitle>
                <CardDescription>{period.periodName}</CardDescription>
            </CardHeader>
        </Card>
    );
};

export default ReportHeader;
