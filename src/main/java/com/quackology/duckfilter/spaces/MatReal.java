package com.quackology.duckfilter.spaces;

import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.algo.decomposition.DenseDoubleQRDecomposition;

/**
 * Wrapper class for 2D matrix of real numbers
 * <p>
 * Based on the parallel colt library
 */
public class MatReal implements Mat<MatReal> {

    /**
     * Algebra object for matrix operations
     */
    private static final DenseDoubleAlgebra ALGEBRA = new DenseDoubleAlgebra(1e-12);

    /**
     * Values of the matrix
     */
    private DoubleMatrix2D value;

    /**
     * Constructor of a matrix based on the parallel colt library matrix
     * 
     * @param matrix DoubleMatrix2D from the parallel colt library containing the values of the matrix
     */
    public MatReal(DoubleMatrix2D matrix) {
        this.value = matrix;
    }

    /**
     * Constructor of a matrix with a single value
     * 
     * @param value Value of only element in the matrix
     */
    public MatReal(double value) {
        this.value = DoubleFactory2D.dense.make(new double[][] {{value}});
    }

    /**
     * Constructor of a matrix with a 2D array of doubles
     * 
     * @param matrix 2D array of doubles containing the values of the matrix
     */
    public MatReal(double matrix[][]) {
        this.value = DoubleFactory2D.dense.make(matrix);
    }

    /**
     * Gets a 2D array of doubles representing the matrix values
     * 
     * @return a 2D array of doubles representing the matrix values
     */
    public double[][] get() {
        return this.value.toArray();
    }

    /**
     * Gets the value at the given row and column
     * 
     * @param row row of the value
     * @param column column of the value
     * @return the value at the given row and column
     */
    public double get(int row, int column) {
        return this.value.get(row, column);
    }
    
    @Override
    public int getRows() {
        return this.value.rows();
    }

    @Override
    public int getCols() {
        return this.value.columns();
    }

    @Override
    public MatReal getRow(int row) {
        return new MatReal(this.value.viewPart(row, 0, 1, this.getCols()));
    }

    @Override
    public MatReal getCol(int column) {
        return new MatReal(this.value.viewPart(0, column, this.getRows(), 1));
    }

    @Override
    public MatReal toVector() {
        return new MatReal(new double[][] {this.value.vectorize().toArray()}).transpose();
    }

    @Override
    public MatReal transpose() {
        return new MatReal(this.value.viewDice());
    }

    @Override
    public MatReal minor(int row, int column) {
        this.value.get(row, column);
        return new MatReal(DoubleFactory2D.dense.compose(new DoubleMatrix2D[][] {
            {this.value.viewPart(0, 0, row, column), this.value.viewPart(0, column+1, row, this.getCols()-column-1)},
            {this.value.viewPart(row+1, 0, this.getRows()-row-1, column), this.value.viewPart(row+1, column+1, this.getRows()-row-1, getCols()-column-1)}
        }));
    }

    @Override
    public MatReal subMat(int row, int column, int height, int width) {
        return new MatReal(this.value.viewPart(row, column, height, width));
    }

    /**
     * Gets a new matrix with the value at the given row and column set to the given value
     * 
     * @param row row of element getting replaced
     * @param column column of element getting replaced
     * @param value value to replace the element with
     * @return a new matrix with the value at the given row and column set to the given value
     */
    public MatReal set(int row, int column, double value) {
        double[][] out = this.value.toArray();
        out[row][column] = value;
        return new MatReal(out);
    }

    @Override
    public MatReal add(MatReal matrix) {
        if (getRows() != matrix.getRows() || this.getCols() != matrix.getCols()) {
            throw new IllegalArgumentException("Matrices must be the same size");
        }
        
        double[][] out = this.value.toArray();

        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                out[i][j] += matrix.value.get(i, j);
            }
        }
        return new MatReal(out);
    }

    @Override
    public MatReal subtract(MatReal matrix) {
        if (getRows() != matrix.getRows() || this.getCols() != matrix.getCols()) {
            throw new IllegalArgumentException("Matrices must be the same size");
        }
        
        double[][] out = this.value.toArray();

        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                out[i][j] -= matrix.value.get(i, j);
            }
        }
        return new MatReal(out);
    }

    @Override
    public MatReal multiply(MatReal matrix) {
        return new MatReal(this.value.zMult(matrix.value, null));
    }

    @Override
    public MatReal multiply(double scalar) {
        double[][] out = this.value.toArray();

        for(int i = 0; i < this.getRows(); i++) {
            for(int j = 0; j < this.getCols(); j++) {
                out[i][j] *= scalar;
            }
        }

        return new MatReal(out);
    }

    /**
     * Gets the determinant of the matrix 
     * 
     * @return the determinant of the matrix
     */
    public double determinant() {
        return ALGEBRA.det(this.value);
    }

    /**
     * Gets the inverse of the matrix
     * 
     * @return the inverse of the matrix
     */
    public MatReal inverse() {
        return new MatReal(ALGEBRA.inverse(this.value));
    }

    /**
     * Calculates the lower triangular matrix of the Cholesky decomposition
     * 
     * @return the lower triangular matrix of the Cholesky decomposition
     */
    public MatReal choleskyDecompose() {
        return new MatReal(ALGEBRA.chol(this.value).getL());
    }

    /**
     * Calculates the upper triangular QR decomposition of the matrix
     * 
     * @return an array containing the Q and R matrices respectively from the QR decomposition of the matrix
     */
    public MatReal[] QRDecomposition() {
        DenseDoubleQRDecomposition out = ALGEBRA.qr(this.value);
        return new MatReal[] {new MatReal(out.getQ(true)), new MatReal(out.getR(true))};
    }

    /**
     * Gets the trace of the matrix
     * 
     * @return the trace of the matrix
     */
    public double trace() {
        return ALGEBRA.trace(this.value);
    }

    public String toString() {
        return this.value.toString();
    }

    /**
     * Forms an identity matrix of the given size
     * 
     * @param size the size of the identity matrix (size x size)
     * @return the identity matrix of the given size
     */
    public static MatReal identity(int size) {
        return new MatReal(DoubleFactory2D.dense.identity(size));
    }

    /**
     * Forms a matrix of zeros with the given amount of rows and columns
     * 
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @return a matrix of the given size with all elements set to 0
     */
    public static MatReal empty(int rows, int columns) {
        return new MatReal(new double[rows][columns]);
    }

    /**
     * Forms a matrix by placing the given matrices diagonally
     * <p>
     * Unfilled areas are filled with zeros
     * 
     * @param matrices array of the matrices to be combined
     * @return a matrix formed by placing the given matrices along the diagonal
     */
    public static MatReal diagonal(MatReal[] matrices) {
        DoubleMatrix2D out = matrices[0].value;
        for(int i = 1; i < matrices.length; i++) {
            out = DoubleFactory2D.dense.composeDiagonal(out, matrices[i].value);
        }
        return new MatReal(out);
    }

    /**
     * Forms a matrix by placing the given matrices horizontally
     * <p>
     * Unfilled areas are filled with zeros
     * 
     * @param matrices array of the matrices to be combined
     * @return a matrix formed by placing the given matrices horizontally
     */
    public static MatReal horizontal(MatReal[] matrices) {
        DoubleMatrix2D out = matrices[0].value;
        for(int i = 1; i < matrices.length; i++) {
            out = DoubleFactory2D.dense.appendColumns(out, matrices[i].value);
        }
        return new MatReal(out);
    }

    /**
     * Forms a matrix by placing the given matrices vertically
     * <p>
     * Unfilled areas are filled with zeros
     * 
     * @param matrices array of the matrices to be combined
     * @return a matrix formed by placing the given matrices vertically
     */
    public static MatReal vertical(MatReal[] matrices) {
        DoubleMatrix2D out = matrices[0].value;
        for(int i = 1; i < matrices.length; i++) {
            out = DoubleFactory2D.dense.appendRows(out, matrices[i].value);
        }
        return new MatReal(out);
    }

    /**
     * Solves for L + beta*vector*vector.transpose() for every vector in W
     * 
     * @param L lower triangular matrix to add to
     * @param W matrix of vectors to be added
     * @param beta scalar to multiply the vectors by
     * @return the sum of matrix L and beta*vector*vector.transpose() for every vector in W
     */
    public static MatReal cholUpdate(MatReal L, MatReal W, double beta) {
        if (L.getRows() != L.getCols()) {
            throw new IllegalArgumentException("Matrix must be square");
        }
        
        double out[][] = new double[L.getRows()][L.getCols()];
        double[][] currentL = L.value.toArray();
        double[][] currentW = new double[W.getRows()][W.getCols()];
        for (int i = 0; i < W.getRows(); i++) {
            for (int j = 0; j < W.getCols(); j++) {
                currentW[i][j] = W.value.toArray()[i][j];
            }
        }

        for (int i = 0; i < W.getCols(); i++) {

            out = new double[L.getRows()][L.getCols()];
            double b = 1;

            for (int j = 0; j < L.getRows(); j++) {

                out[j][j] = Math.sqrt(currentL[j][j]*currentL[j][j] + (beta / b) * currentW[j][i]*currentW[j][i]);
                double upsilon = (currentL[j][j]*currentL[j][j] * b) + (beta * currentW[j][i]*currentW[j][i]);

                for (int k = j+1; k < L.getRows(); k++) {
                    currentW[k][i] -= (currentW[j][i] / currentL[j][j]) * currentL[k][j];
                    out[k][j] = ((out[j][j] / currentL[j][j]) * currentL[k][j]) + (out[j][j] * beta * currentW[j][i] * currentW[k][i] / upsilon);
                }

                b += beta * (currentW[j][i]*currentW[j][i] / currentL[j][j]/currentL[j][j]);
            }

            currentL = out;
        }
        return new MatReal(out);
    }

    /**
     * Solves for x in the equation Ax = b
     * <p>
     * A is lower triangular
     * 
     * @param A lower triangular matrix A
     * @param b vector b
     * @return the solution x to the equation Ax = b
     */
    public static MatReal forwardSub(MatReal A, MatReal b) {
        double[][] out = new double[b.getRows()][b.getCols()];

        for (int k = 0; k < b.getCols(); k++) {
            for (int i = 0; i < A.getRows(); i++) {
                double sum = b.value.toArray()[i][k];
                for (int j = 0; j < i; j++) {
                    sum -= A.value.toArray()[i][j] * out[j][k];
                }

                out[i][k] = sum / A.value.toArray()[i][i];
            }
        }

        return new MatReal(out);
    }

    /**
     * Solves for x in the equation Ax = b
     * <p>
     * A is upper triangular
     * 
     * @param A upper triangular matrix A
     * @param b vector b
     * @return the solution x to the equation Ax = b
     */
    public static MatReal backwardSub(MatReal A, MatReal b) {
        double[][] out = new double[b.getRows()][b.getCols()];

        for (int k = 0; k < b.getCols(); k++) {
            for (int i = A.getRows()-1; i >= 0; i--) {
                double sum = b.value.toArray()[i][k];
                for (int j = A.getRows()-1; j >= i+1; j--) {
                    sum -= A.value.toArray()[i][j] * out[j][k];
                }

                out[i][k] = sum / A.value.toArray()[i][i];
            }
        }

        return new MatReal(out);
    }
}
