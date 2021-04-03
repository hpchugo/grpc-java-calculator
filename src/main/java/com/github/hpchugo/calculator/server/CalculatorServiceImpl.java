package com.github.hpchugo.calculator.server;

import com.proto.calculator.*;
import io.grpc.stub.StreamObserver;


public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
    @Override
    public void calculatorManyTime(CalculatorManyTimesRequest request, StreamObserver<CalculatorManyTimesResponse> responseObserver) {
        int number = request.getCalculator().getFactor();
        int divisor = 2;
        while (number > 1) {
            if (number % divisor == 0) {
                number = number / divisor;
                responseObserver.onNext(CalculatorManyTimesResponse.newBuilder()
                        .setResult(divisor)
                        .build());
            } else {
                divisor = divisor + 1;
            }
        }
        responseObserver.onCompleted();
    }
}
