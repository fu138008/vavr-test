package org.acme;


import io.agroal.api.AgroalDataSource;
import io.quarkus.test.junit.QuarkusTest;
import io.vavr.API;
import io.vavr.Predicates;
import io.vavr.collection.Seq;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;


@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testHelloEndpoint() {
        try{
            Assertions.assertTrue(true);
            Connection testConnect = Mockito.mock(Connection.class);
            PreparedStatement testStatement = Mockito.mock(PreparedStatement.class);
            ResultSet testRs = Mockito.mock(ResultSet.class);
//            Mockito.when(testConnect.prepareStatement(Mockito.anyString())).thenReturn(testStatement);
            Mockito.when(testConnect.prepareStatement(Mockito.anyString())).thenThrow(new SQLException());
            Mockito.when(testStatement.executeQuery()).thenReturn(testRs);
//            Mockito.when(testStatement.executeQuery()).thenThrow(new SQLException());
//            fp_testConnect(testConnect);
            fp_testErrConnect(testConnect);
//            Either<Throwable,ResultSet> rq =  tryErrPreparedStatement(testConnect).flatMap((e) -> tryErrSetParameter(e,"")).flatMap(this::tryErrQuery);
//            if(rq.isLeft()) {
//                System.out.println(rq.getLeft().getCause());
//                System.out.println(rq.getLeft().getMessage());
//            }
        }catch(Throwable e)
        {
            System.out.println(e.getCause());
            System.out.println(e.getMessage());
        }
    }


    public ResultSet fp_testConnect(Connection Conn) throws Exception{
        Try<PreparedStatement> preparedStatement = Try.of(() -> Conn.prepareStatement(""));
        Either<Exception,ResultSet> myPipe =  tryPreparedStatement(Conn).flatMap((e) -> trySetParameter(e,"")).flatMap(this::tryQuery);

        if(myPipe.isRight())
            return myPipe.get();
        throw myPipe.getLeft();
    }

    public Either<Exception,PreparedStatement> tryPreparedStatement(Connection conn){
        Try<PreparedStatement> preparedStatement = Try.of(() -> conn.prepareStatement(""));
        if(preparedStatement.isSuccess())
            return Either.right(preparedStatement.get());
        return Either.left(new SQLException("PrepareErr"));
    }

    public Either<Exception,PreparedStatement> trySetParameter(PreparedStatement preparedStatement,String a){
        try{
            preparedStatement.setString(1,a);
            return Either.right(preparedStatement);
        }catch(SQLException e)
        {
            return Either.left(new SQLException("SetErr"));
        }
    }

    public Either<Exception,ResultSet> tryQuery(PreparedStatement preparedStatement){
        Try<ResultSet> rs = Try.of(() -> preparedStatement.executeQuery());
//        rs.mapFailure(
//            API.Case(API.$(Predicates.instanceOf(SQLTimeoutException.class)),new Exception("TimeoutErr")),
//            API.Case(API.$(Predicates.instanceOf(SQLException.class)),new Exception("QueryErr"))
//        );
        if(rs.isSuccess())
            return Either.right(rs.get());
        return Either.left(new SQLException("QueryErr"));
    }



    public ResultSet fp_testErrConnect(Connection Conn) throws Throwable{
        Either<Throwable,ResultSet> myPipe =  tryErrPreparedStatement(Conn).flatMap((e) -> tryErrSetParameter(e,"")).flatMap(this::tryErrQuery);

        if(myPipe.isRight())
            return myPipe.get();
        throw myPipe.getLeft();
    }

    public Either<Throwable,PreparedStatement> tryErrPreparedStatement(Connection conn){
        Try<PreparedStatement> preparedStatement = Try.of(() -> conn.prepareStatement(""));
        Try<PreparedStatement> mapPreparedStatement = preparedStatement.mapFailure(
                API.Case(API.$(Predicates.instanceOf(SQLException.class)),new Throwable("PrepareErr",preparedStatement.getCause()))
        );
        return mapPreparedStatement.toEither();
    }

    public Either<Throwable,PreparedStatement> tryErrSetParameter(PreparedStatement preparedStatement,String a){
        Try<PreparedStatement> setPara = Try.of(() -> {
            preparedStatement.setString(1,a);
            return preparedStatement;
        });
        Try<PreparedStatement> mapSetPara = setPara.mapFailure(
                API.Case(API.$(Predicates.instanceOf(SQLException.class)),new Throwable("ParaErr",setPara.getCause()))
        );
        return mapSetPara.toEither();
    }

    public Either<Throwable,ResultSet> tryErrQuery(PreparedStatement preparedStatement){
        Try<ResultSet> rs = Try.of(() -> preparedStatement.executeQuery());
        Try<ResultSet> mapRs = rs.mapFailure(
                API.Case(API.$(Predicates.instanceOf(SQLTimeoutException.class)),new Throwable("TimeOutErr",rs.getCause())),
                API.Case(API.$(Predicates.instanceOf(SQLException.class)),new Throwable("QueryErr",rs.getCause()))
        );
        return mapRs.toEither();
    }

    public Try<String> tryGetData(ResultSet rs)
    {
        Try<String> str = Try.of(() -> rs.getString("myTest"));
        Try<String> recoverStr = str.recover(SQLException.class,"");
        return recoverStr;
    }

    public Validation<String,String> validName(String s)
    {
        if(s.isEmpty())
            return Validation.invalid("b");
        return Validation.valid(s);
    }
////    public List<String> testConnect(Connection Conn){
//        try{
//            PreparedStatement preparedStatement = Conn.prepareStatement("");
//            ResultSet rs = preparedStatement.executeQuery();
//        }catch(SQLException e){
//
//        }
//
//
//
//    }
}