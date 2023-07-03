package com.tccnatan.infohealth;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;
import java.util.Calendar;

public class QLearning {
    public double[][] qTable;
    private double learningRate;
    private double discountFactor;
    private double epsilon;
    private int numStates;
    private int numActions;

    public int diadaSemana;

    private FirebaseUser user;

    private DatabaseReference myRef;



    public QLearning(int numStates, int numActions, double learningRate, double discountFactor, double epsilon, int diadaSemana,FirebaseUser user, DatabaseReference myRef,double[][] qtable_firebase ) {
        this.numStates = numStates;
        this.numActions = numActions;
        this.learningRate = learningRate;
        this.discountFactor = discountFactor;
        this.epsilon = epsilon;
        this.qTable = qtable_firebase;
        this.diadaSemana = diadaSemana;

        //Firebase save
        this.user = user;
        this.myRef = myRef;


    }

    public int chooseAction(int state) {
        // Lógica para escolher uma ação com base na estratégia de exploração ou exploração
        // Retorne o índice da ação escolhida

        Random rand = new Random();

        // Se um valor aleatório entre 0 e 1 for menor que epsilon,
        // escolha o valor mais promissor da tabela Q para este estado.
        if (rand.nextDouble() < epsilon) {
            //System.out.println("Escolhendo Ação no Estado: "+state );
            double[] row = qTable[state];
            int maxIndex = 0;
            for (int i = 1; i < row.length; i++) {
                if (row[i] > row[maxIndex]) {
                    maxIndex = i;
                }
            }
            return maxIndex;
        } else {  // escolha uma ação aleatória
            return rand.nextInt(numActions);
        }

    }

    public void updateQValue(int state, int action, double reward, int nextState) {
        double currentQValue = qTable[state][action];
        double maxNextQValue = getMaxQValue(nextState);
        double newQValue = currentQValue + learningRate * (reward + discountFactor * maxNextQValue - currentQValue);
        qTable[state][action] = newQValue;
        myRef.child("Users").child(user.getUid()).child("Qtable").child(""+state+"_"+action).setValue(newQValue);
    }

    private double getMaxQValue(int state) {
        double maxQValue = Double.NEGATIVE_INFINITY;
        for (int action = 0; action < numActions; action++) {
            double qValue = qTable[state][action];
            if (qValue > maxQValue) {
                maxQValue = qValue;
            }
        }
        return maxQValue;
    }

    public double getReward(int state, int action, int flag_resposta) {
        // Implemente a lógica para calcular a recompensa com base no estado atual e ação tomada
        // Retorne o valor da recompensa

        double reward = 0;

        if(action == 1){
           reward = -1;
        }
        else{
            if(flag_resposta == 1 ){
                reward= 100;
                System.out.println("Respondeu..");
            }
            /*if(state == 60 || state == 132 || state == 204 || state == 276 || state == 348 || state == 420 ){
                reward= 100;
            }*/
            else{
                reward =  -20;
            }
        }
        return reward;
    }

    public int getNextState(int state, int action, int nStates, int local) {
        // Implemente a lógica para determinar o próximo estado com base no estado atual e ação tomada
        // Retorne o índice do próximo estado

        int nextstate = 0;

        Calendar calendario = Calendar.getInstance();

        diadaSemana = calendario.get(Calendar.DAY_OF_WEEK);

        switch (diadaSemana) {
            case Calendar.SUNDAY:
                diadaSemana = 0;
                break;
            case Calendar.MONDAY:
                diadaSemana = 1;
                break;
            case Calendar.TUESDAY:
                diadaSemana = 2;
                break;
            case Calendar.WEDNESDAY:
                diadaSemana = 3;
                break;
            case Calendar.THURSDAY:
                diadaSemana = 4;
                break;
            case Calendar.FRIDAY:
                diadaSemana = 5;
                break;
            case Calendar.SATURDAY:
                diadaSemana = 6;
                break;
        }

        //System.out.println("Dia da Semana>: " + diadaSemana);

        int horas = calendario.get(Calendar.HOUR_OF_DAY);
        //int minutos = calendario.get(Calendar.MINUTE);
        //int segundos = calendario.get(Calendar.SECOND);

        //System.out.println("Horas: " + horas);
        //System.out.println("Minutos: " + minutos);
        //System.out.println("Segundos: " + segundos);


        /*
        if(0<=segundos && segundos<=24){
            //System.out.println("Segundos: "+segundos);

            if(segundos == 24){
                try {
                    Thread.sleep(1000); // Atraso de 1 segundos
                    // Código a ser executado após o atraso de 5 segundos
                    //qLearning.printQValues();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                diadaSemana = diadaSemana+1;

                if(diadaSemana==7){
                    diadaSemana = 0;
                }
                //System.out.println("Dia da Semana: "+diadaSemana);
                nextstate = state;
            }
            else{
                if((0<=segundos && segundos<7) || (19<=segundos && segundos<=23) ){

                    local = 0;
                }
                else if((7<=segundos && segundos<9) || (18<=segundos && segundos<19)){
                    local = 2;
                }
                else{
                    local = 1;
                }

                nextstate = (((segundos * 3)+ local )+ ( diadaSemana * 72));
            }

        }
        else{
            nextstate = state;
        }*/

        //System.out.println(latitude);
        //System.out.println(longitude);

        //System.out.println("State: "+state + " Local: "+local);
        // Fórmula certa com 504 estados ( localização + dia + horas)

        nextstate = ((horas * 3)+ local )+ ( diadaSemana * 72);

        return  nextstate;



    }




    public void printQValues(){
        System.out.print("QTable");

        for (int i = 0; i < qTable.length; i++) {
            for (int j = 0; j < qTable[i].length; j++) {
                String formato = String.format("%.1f",qTable[i][j]);
                System.out.print( formato + "\t");
            }
            System.out.println();
        }

    }

    public static void main(String[] args) {
        int numStates = 10;
        int numActions = 4;
        double learningRate = 0.1;
        double discountFactor = 0.9;
        double epsilon = 0.9;

        FirebaseUser user;

        DatabaseReference myRef;

        FirebaseAuth mAuth;
        FirebaseDatabase database;

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        user = mAuth.getCurrentUser();
        myRef = database.getReference();

        double[][] qtable_firebase = new double[numStates][numActions];




        QLearning qLearning = new QLearning(numStates, numActions, learningRate, discountFactor, epsilon,0, user, myRef, qtable_firebase);

        int currentState = 0;
        int maxIterations = 100;
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            int action = qLearning.chooseAction(currentState);

            // Simule tomar a ação escolhida e observe a recompensa e o próximo estado
            double reward = qLearning.getReward(currentState, action,0);  // Função de recompensa
            int nextState = qLearning.getNextState(currentState, action, numStates, 0);  // Função de próximo estado

            qLearning.updateQValue(currentState, action, reward, nextState);

            qLearning.printQValues();

            currentState = nextState;
        }
    }


}
