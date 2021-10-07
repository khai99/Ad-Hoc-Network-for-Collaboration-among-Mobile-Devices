package com.group10b.blueka.Operation;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This is the Manager of all operations, this will be used by the advertiser and scanner for to
 * operated their operations sequentially. It contains an queue to make the operations work in a sequence.
 */
public class OperationManager {
    private Queue<Operation> operations = new LinkedList<>();
    private Operation currentOp = null;
    public OperationManager(){

    }

    /**
     * To request a operation to be execute.
     * @param operation The operation need to be executed.
     */
    public synchronized void request(Operation operation){
        operations.add(operation);
        if(currentOp == null){
            currentOp = operations.poll();
            currentOp.performOperation();

        }
    }

    /**
     * To tell the Manager the operation has completed. The Manager will dequeue the next operation to execute.
     */
    public synchronized void operationCompleted(){
        currentOp = null;
        if(operations.peek() != null){
            currentOp = operations.poll();
            currentOp.performOperation();
        }
    }
}
