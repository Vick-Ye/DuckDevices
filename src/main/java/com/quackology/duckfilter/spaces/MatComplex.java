package com.quackology.duckfilter.spaces;

import cern.colt.matrix.tdcomplex.DComplexFactory2D;
import cern.colt.matrix.tdcomplex.DComplexMatrix2D;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;

/**
 * Wrapper class for 2D matrix of complex numbers
 * <p>
 * Based on the parallel colt library
 */
public class MatComplex implements Mat<MatComplex> {

    /**
     * Values 
     */
    private DComplexMatrix2D value;

    /**
     * Constructor of a matrix based on the parallel colt library matrix
     * 
     * @param matrix DoubleMatrix2D from the parallel colt library
     */
    public MatComplex(DComplexMatrix2D matrix) {
        this.value = matrix;
    }

    /**
     * Constructor of a matrix with matrices of real and imaginary parts based on the parallel colt library
     * 
     * @param real DoubleMatrix2D from the parallel colt library containing the real part of the matrix
     * @param img DoubleMatrix2D from the parallel colt library containing the imaginary part of the matrix
     */
    public MatComplex(DoubleMatrix2D real, DoubleMatrix2D img) {
        if (real.rows() != img.rows() || real.columns() != img.columns()) {
            throw new IllegalArgumentException("Real and imaginary parts must be the same size");
        }
        this.value = DComplexFactory2D.dense.make(real.rows(), real.columns());
        this.value.assignReal(real);
        this.value.assignImaginary(img);
    }

    /**
     * Constructor of a matrix with a 2D array of complex numbers
     * 
     * @param matrix 2D array of complex numbers containing the values of the matrix
     */
    public MatComplex(Complex[][] matrix) {
        this.value = DComplexFactory2D.dense.make(matrix.length, matrix[0].length);
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                this.value.set(i, j, matrix[i][j].getReal(), matrix[i][j].getImg());
            }
        }
    }

    /**
     * Constructor of a matrix with a single complex number
     * 
     * @param real Real part of the only element in the matrix
     * @param img Imaginary part of the only element in the matrix
     */
    public MatComplex(double real, double img) {
        this.value = DComplexFactory2D.dense.make(1, 1);
    }

    /**
     * Constructor of a matrix with matrices of real and imaginary parts based on MatReal
     * 
     * @param real MatReal containing the real part of the matrix
     * @param img MatReal containing the imaginary part of the matrix
     */
    public MatComplex(MatReal real, MatReal img) {
        this(real.get(), img.get());
    }

    /**
     * Constructor of a matrix with 2D arrays of doubles of real and imaginary parts
     * 
     * @param real 2D array of doubles containing the real part of the matrix
     * @param img 2D array of doubles containing the imaginary part of the matrix
     */
    public MatComplex(double[][] real, double[][] img) {
        this(DoubleFactory2D.dense.make(real), DoubleFactory2D.dense.make(img));
    }

    /**
     * Gets a 2D array of complex numbers containing the values of the matrix
     * 
     * @return a 2D array of complex numbers containing the values of the matrix
     */
    public Complex[][] get() {
        Complex[][] out = new Complex[this.getRows()][this.getCols()];
        for (int i = 0; i < this.getRows(); i++) {
            for (int j = 0; j < this.getCols(); j++) {
                out[i][j] = new Complex(this.value.get(i, j)[0], this.value.get(i, j)[1]);
            }
        }
        return out;
    }

    /**
     * Gets the value at the given row and column
     * 
     * @param row row of the value
     * @param column column of the value
     * @return the value at the given row and column
     */
    public Complex get(int row, int column) {
        return new Complex(this.value.get(row, column)[0], this.value.get(row, column)[1]);
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
    public MatComplex getRow(int row) {
        return new MatComplex(this.value.viewPart(row, 0, 1, this.getCols()));
    }

    @Override
    public MatComplex getCol(int column) {
        return new MatComplex(this.value.viewPart(0, column, this.getRows(), 1));
    }

    @Override
    public MatReal toVector() {
        return MatReal.vertical(new MatReal[] {new MatReal(this.value.getRealPart().toArray()).toVector(), new MatReal(this.value.getImaginaryPart().toArray()).toVector()});
    }

    @Override
    public MatComplex transpose() {
        return new MatComplex(this.value.viewDice());
    }

    @Override
    public MatComplex subMat(int row, int column, int height, int width) {
        return new MatComplex(this.value.viewPart(row, column, height, width));
    }

    /**
     * Gets a new matrix with the value at the given row and column set to the complex value of the given real and imaginary parts
     * 
     * @param row row of the element getting replaced
     * @param column column of the element getting replaced
     * @param real real part of complex number to replace the element with
     * @param img imaginary part of complex number to replace the element with
     * @return a new matrix with the value at the given row and column set to the complex value of the given real and imaginary parts
     */
    public MatComplex set(int row, int column, double real, double img) {
        Complex[][] out = this.get();
        out[row][column] = new Complex(real, img);
        return new MatComplex(out);
    }

    /**
     * Gets a new matrix with the value at the given row and column set to the given complex value
     * 
     * @param row row of the element getting replaced
     * @param column column of the element getting replaced
     * @param value complex number to replace the element with
     * @return a new matrix with the value at the given row and column set to the given complex value
     */
    public MatComplex set(int row, int column, Complex value) {
        Complex[][] out = this.get();
        out[row][column] = value;
        return new MatComplex(out);
    }

    @Override
    public MatComplex add(MatComplex matrix) {
        MatComplex out = new MatComplex(this.value.copy());
        out.value.assign(matrix.value, (a, b) -> new double[] {a[0] + b[0], a[1] + b[1]});
        return out;
    }

    @Override
    public MatComplex subtract(MatComplex matrix) {
        MatComplex out = new MatComplex(this.value.copy());
        out.value.assign(matrix.value, (a, b) -> new double[] {a[0] - b[0], a[1] - b[1]});
        return out;
    }

    @Override
    public MatComplex multiply(MatComplex matrix) {
        return new MatComplex(this.value.zMult(matrix.value, null));
    }

    @Override
    public MatComplex multiply(double scalar) {
        Complex[][] out = this.get();

        for(int i = 0; i < this.getRows(); i++) {
            for(int j = 0; j < this.getCols(); j++) {
                out[i][j] = out[i][j].multiply(scalar);
            }
        }

        return new MatComplex(out);
    }

    /**
     * Unimplemented
     * <p>
     * return the determinant of the matrix
     * 
     * @throws UnsupportedOperationException
     * @return the determinant of the matrix
     */
    public double determinant() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Unimplemented
     * <p>
     * return the inverse of the matrix
     * 
     * @throws UnsupportedOperationException
     * @return the inverse of the matrix
     */
    public MatComplex inverse() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Gets the trace of the matrix
     * 
     * @return the trace of the matrix
     */
    public Complex trace() {
        Complex out = new Complex(0, 0);
        for(int i = 0; i < Math.min(this.getRows(), this.getCols()); i++) {
            out = out.add(this.get(i, i));
        }
        return out;
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
    public static MatComplex identity(int size) {
        return new MatComplex(DComplexFactory2D.dense.identity(size));
    }

    /**
     * Forms a matrix of zeros with the given amount of rows and columns
     * 
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @return a matrix of the given size with all elements set to 0
     */
    public static MatComplex empty(int rows, int columns) {
        return new MatComplex(new double[rows][columns], new double[rows][columns]);
    }

    /**
     * Forms a matrix by placing the given matrices diagonally
     * <p>
     * Unfilled areas are filled with zeros
     * 
     * @param matrices array of the matrices to be combined
     * @return a matrix formed by placing the given matrices along the diagonal
     */
    public static MatComplex diagonal(MatComplex[] matrices) {
        DComplexMatrix2D out = matrices[0].value;
        for(int i = 1; i < matrices.length; i++) {
            out = DComplexFactory2D.dense.composeDiagonal(out, matrices[i].value);
        }
        return new MatComplex(out);
    }

    /**
     * Forms a matrix by placing the given matrices horizontally
     * <p>
     * Unfilled areas are filled with zeros
     * 
     * @param matrices array of the matrices to be combined
     * @return a matrix formed by placing the given matrices horizontally
     */
    public static MatComplex horizontal(MatComplex[] matrices) {
        DComplexMatrix2D out = matrices[0].value;
        for(int i = 1; i < matrices.length; i++) {
            out = DComplexFactory2D.dense.appendColumns(out, matrices[i].value);
        }
        return new MatComplex(out);
    }

    /**
     * Forms a matrix by placing the given matrices vertically
     * <p>
     * Unfilled areas are filled with zeros
     * 
     * @param matrices array of the matrices to be combined
     * @return a matrix formed by placing the given matrices vertically
     */
    public static MatComplex vertical(MatComplex[] matrices) {
        DComplexMatrix2D out = matrices[0].value;
        for(int i = 1; i < matrices.length; i++) {
            out = DComplexFactory2D.dense.appendRows(out, matrices[i].value);
        }
        return new MatComplex(out);
    }
}
