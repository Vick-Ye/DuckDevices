package com.quackology.duckdevices.spaces;

import org.ojalgo.matrix.MatrixC128;
import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.store.GenericStore;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.scalar.ComplexNumber;

/**
 * Wrapper class for 2D matrix of complex numbers
 * <p>
 * Based on OjAlgo's ComplexMatrix interface (MatrixC128)
 */
public class MatComplex implements Mat<MatComplex> {

	/**
	 * OjAlgo's factory for creating matrices
	 */
	private static final MatrixC128.Factory MATRIX_FACTORY = MatrixC128.FACTORY;
    private static final PhysicalStore.Factory<ComplexNumber, ?> STORE_FACTORY = GenericStore.C128;
    /**
     * Values of the matrix
     */
	private final MatrixC128 value;

    /**
     * Constructor of a matrix based on the OjAlgo MatrixC128
     * 
     * @param matrix MatrixC128 from OjAlgo containing the values of the matrix
     */
    private MatComplex(MatrixC128 matrix) {
        this.value = matrix;
    }

    /**
     * Constructor of a matrix based on the OjAlgo MatrixStore
     *
     * @param matrix MatrixStore from OjAlgo containing the values of the matrix
     */
    private MatComplex(MatrixStore<ComplexNumber> matrix) {
        this.value = MATRIX_FACTORY.copy(matrix);
    }

    /**
     * Constructor of a matrix with real matrices representing real and imaginary parts based on OjAlgo's MatrixR064
     * 
     * @param real MatrixR064 from ojAlgo containing the real part of the matrix
     * @param img MatrixR064 containing the imaginary part of the matrix
     */
    private MatComplex(MatrixR064 real, MatrixR064 img) {
        if (real.getRowDim() != img.getRowDim() || real.getColDim() != img.getColDim()) {
            throw new IllegalArgumentException("Real and imaginary parts must be the same size");
        }
        PhysicalStore<ComplexNumber> out = STORE_FACTORY.make(real);
        for (int i = 0; i < out.getRowDim(); i++) {
            for (int j = 0; j < out.getColDim(); j++) {
                out.set(i, j, ComplexNumber.of(real.get(i, j), img.get(i, j)));
            }
        }

        this.value = MATRIX_FACTORY.copy(out);
    }

    /**
     * Constructor of a matrix with real matrices representing real and imaginary parts based on OjAlgo's MatrixStore
     *
     * @param real MatrixR064 from ojAlgo containing the real part of the matrix
     * @param img MatrixR064 containing the imaginary part of the matrix
     */
    private MatComplex(MatrixStore<Double> real, MatrixStore<Double> img) {
        if (real.getRowDim() != img.getRowDim() || real.getColDim() != img.getColDim()) {
            throw new IllegalArgumentException("Real and imaginary parts must be the same size");
        }
        PhysicalStore<ComplexNumber> out = STORE_FACTORY.make(real);
        for (int i = 0; i < out.getRowDim(); i++) {
            for (int j = 0; j < out.getColDim(); j++) {
                out.set(i, j, ComplexNumber.of(real.get(i, j), img.get(i, j)));
            }
        }

        this.value = MATRIX_FACTORY.copy(out);
    }

    /**
     * Constructor of a matrix with a 2D array of complex numbers
     * 
     * @param matrix 2D array of complex numbers containing the values of the matrix
     */
    public MatComplex(Complex[][] matrix) {
        PhysicalStore<ComplexNumber> out = STORE_FACTORY.make(matrix.length, matrix.length == 0 ? 0 : matrix[0].length);
        for (int i = 0; i < out.getRowDim(); i++) {
            for (int j = 0; j < out.getColDim(); j++) {
                out.set(i, j, ComplexNumber.of(matrix[i][j].getReal(), matrix[i][j].getImg()));
            }
        }

        this.value = MATRIX_FACTORY.copy(out);
    }

    /**
     * Constructor of a matrix with a single value
     *
     * @param value Value of only element in the matrix
     */
    public MatComplex(Complex value) {
        this(new Complex[][] {{value}});
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
        if (real.length != img.length || (real.length == 0 ? 0 : real[0].length) != (img.length == 0 ? 0 : img[0].length)) {
            throw new IllegalArgumentException("Real and imaginary parts must be the same size");
        }
        PhysicalStore<ComplexNumber> out = STORE_FACTORY.make(real.length, real.length == 0 ? 0 : real[0].length);
        for (int i = 0; i < out.getRowDim(); i++) {
            for (int j = 0; j < out.getColDim(); j++) {
                out.set(i, j, ComplexNumber.of(real[i][j], img[i][j]));
            }
        }

        this.value = MATRIX_FACTORY.copy(out);
    }

    /**
     * Constructor of a matrix with a single complex number
     *
     * @param real Real part of the only element in the matrix
     * @param img Imaginary part of the only element in the matrix
     */
    public MatComplex(double real, double img) {
        this(new Complex(real, img));
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
                out[i][j] = new Complex(this.value.get(i, j).getReal(), this.value.get(i, j).getImaginary());
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
        return new Complex(this.value.get(row, column).getReal(), this.value.get(row, column).getImaginary());
    }

    public MatReal getReal() {
        return new MatReal(this.value.getReal().toRawCopy2D());
    }

    public MatReal getImg() {
        return new MatReal(this.value.getImaginary().toRawCopy2D());
    }
    
    @Override
    public int getRows() {
        return this.value.getRowDim();
    }

    @Override
    public int getCols() {
        return this.value.getColDim();
    }

    @Override
    public MatComplex getRow(int row) {
        return new MatComplex(this.value.row(row));
    }

    @Override
    public MatComplex getCol(int column) {
        return new MatComplex(this.value.column(column));
    }

    @Override
    public MatReal toVector() {
        MatReal[] vector = new MatReal[this.getCols()*2];
        for (int i = 0; i < this.getCols(); i++) {
            vector[i] = this.getCol(i).getReal();
            vector[i+this.getCols()] = this.getCol(i).getImg();
        }
        return MatReal.vertical(vector);
    }

    @Override
    public MatComplex transpose() {
        return new MatComplex(this.value.transpose());
    }

    @Override
    public MatComplex subMat(int row, int column, int height, int width) {
        return new MatComplex(this.value.offsets(row, column).limits(height, width));
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
        PhysicalStore<ComplexNumber> out = STORE_FACTORY.copy(this.value);
        out.set(row, column, ComplexNumber.of(real, img));
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
        return set(row, column, value.getReal(), value.getImg());
    }

    @Override
    public MatComplex add(MatComplex matrix) {
        return new MatComplex(this.value.add(matrix.value));
    }

    @Override
    public MatComplex subtract(MatComplex matrix) {
        return new MatComplex(this.value.subtract(matrix.value));

    }

    @Override
    public MatComplex multiply(MatComplex matrix) {
        return new MatComplex(this.value.multiply(matrix.value));
    }

    @Override
    public MatComplex multiply(double scalar) {
        return new MatComplex(this.value.multiply(scalar));
    }

    /**
     * Gets the determinant of the matrix
     *
     * @return the determinant of the matrix
     */
    public Complex determinant() {
        ComplexNumber out = this.value.getDeterminant();
        return new Complex(out.getReal(), out.getImaginary());
    }

    /**
     * Gets the inverse of the matrix
     *
     * @return the inverse of the matrix
     */
    public MatComplex inverse() {
        return new MatComplex(this.value.invert());
    }

    /**
     * Gets the trace of the matrix
     * 
     * @return the trace of the matrix
     */
    public Complex trace() {
        ComplexNumber trace = this.value.getTrace();
        return new Complex(trace.getReal(), trace.getImaginary());
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
        return new MatComplex(MATRIX_FACTORY.makeEye(size, size));
    }

    /**
     * Forms a matrix of zeros with the given amount of rows and columns
     * 
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @return a matrix of the given size with all elements set to 0
     */
    public static MatComplex empty(int rows, int columns) {
        return new MatComplex(MATRIX_FACTORY.make(rows, columns));
    }

    /**
     * Forms a matrix by placing the given matrices diagonally
     * <p>
     * Unfilled areas are filled with zeros
     * 
     * @param matrices the matrices to be combined
     * @return a matrix formed by placing the given matrices along the diagonal
     */
    public static MatComplex diagonal(MatComplex... matrices) {
        PhysicalStore<ComplexNumber> out = STORE_FACTORY.copy(matrices[0].value);
        for (int i = 1; i < matrices.length; i++) {
            out = STORE_FACTORY.copy(out.diagonally(matrices[i].value));
        }
        return new MatComplex(out);
    }

    /**
     * Forms a matrix by placing the given matrices horizontally
     * <p>
     * Unfilled areas are filled with zeros
     * 
     * @param matrices the matrices to be combined
     * @return a matrix formed by placing the given matrices horizontally
     */
    public static MatComplex horizontal(MatComplex... matrices) {
        PhysicalStore<ComplexNumber> out = STORE_FACTORY.copy(matrices[0].value);
        for (int i = 1; i < matrices.length; i++) {
            out = STORE_FACTORY.copy(out.right(matrices[i].value));
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
    public static MatComplex vertical(MatComplex... matrices) {
        PhysicalStore<ComplexNumber> out = STORE_FACTORY.copy(matrices[0].value);
        for (int i = 1; i < matrices.length; i++) {
            out = STORE_FACTORY.copy(out.below(matrices[i].value));
        }
        return new MatComplex(out);
    }
}
