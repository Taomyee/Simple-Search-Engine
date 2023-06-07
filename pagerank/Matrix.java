package pagerank;

public class Matrix {
    public double[][] matrix;
    public int row;
    public int column;
    public Matrix(int row, int column){
        this.matrix = new double[row][column];
        this.row = row;
        this.column = column;
    }

    public static Matrix multiply(Matrix A, Matrix B){
        int row = A.row;
        int column = B.column;
        Matrix result = new Matrix(row, column);
        for (int i = 0; i < row; i++){
            for (int j = 0; j < column; j++){
                for (int k = 0; k < A.column; k++){
                    result.matrix[i][j] += A.matrix[i][k] * B.matrix[k][j];
                }
            }
        }
        return result;
    }

    public static Matrix add(Matrix A, Matrix B){
        int row = A.row;
        int column = B.column;
        Matrix result = new Matrix(row, column);
        for (int i = 0; i < row; i++){
            for (int j = 0; j < column; j++){
                result.matrix[i][j] = A.matrix[i][j] + B.matrix[i][j];
            }
        }
        return result;
    }

    public static Matrix subtract(Matrix A, Matrix B){
        int row = A.row;
        int column = B.column;
        Matrix result = new Matrix(row, column);
        for (int i = 0; i < row; i++){
            for (int j = 0; j < column; j++){
                result.matrix[i][j] = A.matrix[i][j] - B.matrix[i][j];
            }
        }
        return result;
    }


    public static double sum(Matrix A){
        double result = 0;
        for (int i = 0; i < A.row; i++){
            for (int j = 0; j < A.column; j++){
                result += A.matrix[i][j];
            }
        }
        return result;
    }

    public static double vectorsum(double[] A){
        double result = 0;
        for (int i = 0; i < A.length; i++){
            result += A[i];
        }
        return result;
    }

    public static void normalize(Matrix A){ //each row
        int row = A.row;
        int column = A.column;
        double row_sum = 0.0;
        for (int i = 0; i < row; i++){
            row_sum = 0.0;
            for (int j = 0; j < column; j++) {
                row_sum += A.matrix[i][j];
            }
            for (int j = 0; j < column; j++){
                A.matrix[i][j] /= row_sum;
            }
        }
    }

    public static double distance(Matrix A, Matrix B){
        int row = A.row;
        int column = B.column;
        double result = 0.0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                result += Math.pow(A.matrix[i][j] - B.matrix[i][j], 2);
            }
        }
        result = Math.sqrt(result);
        return result;
    }
}
