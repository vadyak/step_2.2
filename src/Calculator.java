import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {
    private String inputExpression;
    private String opnString;

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.run();
    }

    private void run(){
        System.out.println("Введите выражение:");
        enterExpression();
        replaceCommaToDot();

        if (validate()) {
            try {
                toOPN(inputExpression);
                calculate(opnString);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else System.out.println("Ошибка составления выражения");
    }

    private void enterExpression() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))){
            String s = bufferedReader.readLine();
            if (!s.isEmpty()) inputExpression = s;
            else {
                inputExpression = "";
                throw new IOException();
            }
        } catch (IOException e){
            System.out.println("Ошибка считывания выражения");
        }
    }

    private void replaceCommaToDot(){
            String s = inputExpression;
            s = s.replaceAll(",", "."); //замена зарятых на точки
            inputExpression = s;
    }

    private boolean validate(){
        Pattern pattern_1 = Pattern.compile("([0-9\\+\\-\\*\\/\\^\\(\\)\\s\\.]+)");//должны быть только цифры, пробелы и операторы
        Matcher matcher_1 = pattern_1.matcher(inputExpression);
        Pattern pattern_2 = Pattern.compile("\\d+\\.*\\d*\\s+\\d+\\.*\\d*|\\d+\\.*\\d*+\\s*\\(|\\)\\s*\\d+\\.*\\d*");//не должно быть пробелов между несколькими цифрами, без пустого места между скобкой и цифрой
        Matcher matcher_2 = pattern_2.matcher(inputExpression);
        if (!matcher_1.matches() || matcher_2.find()) {
            return false;
        }
        return true;
    }

    private void toOPN(String inputString) throws Exception {//перевод в обратную польскую нотацию
        StringBuilder stack = new StringBuilder("");
        StringBuilder out = new StringBuilder("");
        char charIn, charTmp;
        boolean isPreviousOperator = false;

        for (int i = 0; i < inputString.length(); i++) {
            charIn = inputString.charAt(i);
            if (isOperator(charIn) && !isPreviousOperator && i != 0){
                while (stack.length() > 0){
                    charTmp = stack.charAt(stack.length() - 1);
                    if (isOperator(charTmp) && priorOperator(charIn) <= priorOperator(charTmp)){
                        out.append(" ").append(charTmp);
                        stack.setLength(stack.length() - 1);
                    } else {
                        out.append(" ");
                        break;
                    }
                }
                out.append(" ");
                stack.append(charIn);
                isPreviousOperator = true;
            } else if ((isOperator(charIn) && isPreviousOperator) || (isOperator(charIn) && i == 0)){
                out.append(charIn);
            } else if (charIn == '('){
                stack.append(charIn);
                isPreviousOperator = false;
            } else if (charIn == ')') {
                charTmp = stack.charAt(stack.length() - 1);
                while (charTmp != '('){
                    if (stack.length() < 1) throw new Exception("Ошибка: несогласованность скобок в выражении");
                    out.append(" ").append(charTmp);
                    stack.setLength(stack.length() - 1);
                    charTmp = stack.charAt(stack.length() - 1);
                }
                stack.setLength(stack.length() - 1);
                isPreviousOperator = false;
            } else if (charIn == ' '){
                out.append(charIn);
            } else {
                out.append(charIn);
                isPreviousOperator = false;
            }
        }

        while (stack.length() > 0){
            out.append(" ").append(stack.charAt(stack.length() - 1));
            stack.setLength(stack.length() - 1);
        }

        opnString = out.toString();
    }

    private void calculate(String s) throws Exception {//расчет выражения из обратной польской нотации
        ArrayDeque<BigDecimal> currentStack = new ArrayDeque<>();
        String[] allElements = s.split("\\s+");
        BigDecimal tmpDouble_1;
        BigDecimal tmpDouble_2;
        BigDecimal tmpResult = new BigDecimal("0.0");

        for (int i = 0; i < allElements.length; i++) {
            char charTmp = allElements[i].charAt(0);
            if (allElements[i].length() == 1 && isOperator(charTmp) && currentStack.size() > 1) {
                tmpDouble_1 = currentStack.pop();
                tmpDouble_2 = currentStack.pop();
                tmpResult = toDecide(tmpDouble_1, tmpDouble_2, charTmp);
                currentStack.push(tmpResult);
            } else if (allElements[i].length() == 1 && isOperator(charTmp) && currentStack.size() < 2){
                System.out.println(currentStack);
                throw new Exception("Ошибка: не правильное количество операндов");
            } else currentStack.push(new BigDecimal(allElements[i]));
        }
        System.out.println(tmpResult);
    }

    private BigDecimal toDecide(BigDecimal tmpDouble_1, BigDecimal tmpDouble_2, char operator) throws Exception {//расчет по действиям
        switch (operator){
            case '*':
                return tmpDouble_1.multiply(tmpDouble_2);
            case '/':
                if (tmpDouble_1.compareTo(BigDecimal.ZERO) == 0) throw new Exception("Ошибка: деление на ноль");
                return tmpDouble_2.divide(tmpDouble_1, 3, BigDecimal.ROUND_DOWN);
            case '+':
                return tmpDouble_1.add(tmpDouble_2);
            case '-':
                return tmpDouble_2.subtract(tmpDouble_1);
            case '^':
                return new BigDecimal(Math.pow(tmpDouble_2.doubleValue(), tmpDouble_1.doubleValue())).setScale(3,BigDecimal.ROUND_DOWN);
            default:
                return BigDecimal.ZERO;
        }
    }

    private boolean isOperator(char charIn) {
        switch (charIn){
            case '+':
            case '-':
            case '*':
            case '/':
            case '^':
                return true;
        }
        return false;
    }

    private int priorOperator(char operator){
        switch (operator){
            case '^':
                return 3;
            case '*':
            case '/':
                return 2;
        }
        return 1;
    }
}
