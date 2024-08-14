package com.quackology.duckdevices.spaces;

import org.ojalgo.matrix.MatrixR064;
import org.ojalgo.matrix.decomposition.Cholesky;
import org.ojalgo.matrix.decomposition.QR;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.R064Store;
import org.ojalgo.matrix.store.RawStore;

import javax.annotation.Nonnull;

/**
 * Wrapper class for 2D matrix of real numbers
 * <p>
 * Based on OjAlgo's PrimitiveMatrix interface (MatrixR064)
 */
public class MatReal implements Mat<MatReal> {

	/**
	 * OjAlgo's factory for creating matrices
	 */
	private static final MatrixR064.Factory MATRIX_FACTORY = MatrixR064.FACTORY;
    private static final PhysicalStore.Factory<Double, R064Store> STORE_FACTORY = R064Store.FACTORY;

    /**
     * OjAlgo's solver instances
     */
    private static final Cholesky<Double> CHOLESKY_SOLVER = Cholesky.R064.make();
    private static final QR<Double> QR_SOLVER = QR.R064.make();

    /**
     * Values of the matrix
     */
	private final MatrixR064 value;

    /**
     * Constructor of a matrix based on the OjAlgo MatrixR064
     * 
     * @param matrix MatrixR064 from OjAlgo containing the values of the matrix
     */
    public MatReal(MatrixR064 matrix) {
        this.value = matrix;
    }

    /**
     * Constructor of a matrix based on the OjAlgo MatrixStore
     *
     * @param matrix MatrixR064 from OjAlgo containing the values of the matrix
     */
    public MatReal(MatrixStore<Double> matrix) {
        this.value = MATRIX_FACTORY.copy(matrix);
    }

    /**
     * Constructor of a matrix with a single value
     * 
     * @param value Value of only element in the matrix
     */
    public MatReal(double value) {
		this(new double[][] {{value}});
    }

    /**
     * Constructor of a matrix with a 2D array of doubles
     * 
     * @param matrix 2D array of doubles containing the values of the matrix
     */
    public MatReal(double[][] matrix) {
		this.value = MATRIX_FACTORY.copy(RawStore.wrap(matrix));
    }

    /**
     * Gets a 2D array of doubles representing the matrix values
     * 
     * @return a 2D array of doubles representing the matrix values
     */
    public double[][] get() {
        return this.value.toRawCopy2D();
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
		return this.value.getRowDim();
    }

    @Override
    public int getCols() {
		return this.value.getColDim();
    }

    @Override
    public MatReal getRow(int row) {
		return new MatReal(this.value.row(row));
    }

    @Override
    public MatReal getCol(int column) {
		return new MatReal(this.value.column(column));
    }

    @Override
    public MatReal toVector() {
		MatReal[] vectors = new MatReal[this.getCols()];
		for (int i = 0; i < this.getCols(); i++) {
			vectors[i] = this.getCol(i);
		}
		return MatReal.vertical(vectors);
    }

    @Override
    public MatReal transpose() {
		return new MatReal(this.value.transpose());
    }

    @Override
    public MatReal subMat(int row, int column, int height, int width) {
		return new MatReal(this.value.offsets(row, column).limits(height, width));
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
        R064Store out = STORE_FACTORY.copy(this.value);
        out.set(row, column, value);
        return new MatReal(out);
    }

    @Override
    public MatReal add(MatReal matrix) {
		return new MatReal(this.value.add(matrix.value));
    }

    @Override
    public MatReal subtract(MatReal matrix) {
		return new MatReal(this.value.subtract(matrix.value));
    }

    @Override
    public MatReal multiply(MatReal matrix) {
		return new MatReal(this.value.multiply(matrix.value));
    }

    @Override
    public MatReal multiply(double scalar) {
		return new MatReal(this.value.multiply(scalar));
    }

    /**
     * Gets the determinant of the matrix 
     * 
     * @return the determinant of the matrix
     */
    public double determinant() {
		return this.value.getDeterminant();
    }

    /**
     * Gets the inverse of the matrix
     * 
     * @return the inverse of the matrix
     */
    public MatReal inverse() {
		return new MatReal(this.value.invert());
    }

    /**
     * Calculates the lower triangular matrix of the Cholesky decomposition
     * 
     * @return the lower triangular matrix of the Cholesky decomposition
     */
    public MatReal choleskyDecompose() {
        CHOLESKY_SOLVER.decompose(this.value);
        return new MatReal(CHOLESKY_SOLVER.getL());
    }

    /**
     * Calculates the lower triangular matrix of the Cholesky decomposition
     * 
     * @param solver a Cholesky instance from OjAlgo tuned to the matrix size
     * @return the lower triangular matrix of the Cholesky decomposition
     */
    public MatReal choleskyDecompose(Cholesky<Double> solver) {
        solver.decompose(this.value);
        return new MatReal(solver.getL());
    }

    /**
     * Calculates the upper triangular QR decomposition of the matrix
     * 
     * @return an array containing the Q and R matrices respectively from the QR decomposition of the matrix
     */
    public MatReal[] QRDecompose() {
        QR_SOLVER.decompose(this.value);
        return new MatReal[] {new MatReal(QR_SOLVER.getQ()), new MatReal(QR_SOLVER.getR())};
    }

    /**
     * Calculates the upper triangular QR decomposition of the matrix
     * 
     * @param solver a QR instance from OjAlgo tuned to the matrix size
     * @return an array containing the Q and R matrices respectively from the QR decomposition of the matrix
     */
    public MatReal[] QRDecompose(QR<Double> solver) {
        solver.decompose(this.value);
        return new MatReal[] {new MatReal(solver.getQ()), new MatReal(solver.getR())};
    }

    /**
     * Gets the trace of the matrix
     * 
     * @return the trace of the matrix
     */
    public double trace() {
        return this.value.getTrace();
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
        return new MatReal(MATRIX_FACTORY.makeEye(size, size));
    }

    /**
     * Forms a matrix of zeros with the given amount of rows and columns
     * 
     * @param rows the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @return a matrix of the given size with all elements set to 0
     */
    public static MatReal empty(int rows, int columns) {
        return new MatReal(MATRIX_FACTORY.make(rows, columns));
    }

    /**
     * Forms a matrix by placing the given matrices diagonally
     * <p>
     * Unfilled areas are filled with zeros
     * 
     * @param matrices the matrices to be combined
     * @return a matrix formed by placing the given matrices along the diagonal
     */
    public static MatReal diagonal(@Nonnull MatReal... matrices) {
        R064Store out = STORE_FACTORY.copy(matrices[0].value);
        for (int i = 1; i < matrices.length; i++) {
            out = STORE_FACTORY.copy(out.diagonally(matrices[i].value));
        }
        return new MatReal(out);
    }

    /**
     * Forms a matrix by placing the given matrices horizontally
     * <p>
     * Unfilled areas are filled with zeros
     * 
     * @param matrices the matrices to be combined
     * @return a matrix formed by placing the given matrices horizontally
     */
    public static MatReal horizontal(@Nonnull MatReal... matrices) {
        R064Store out = STORE_FACTORY.copy(matrices[0].value);
        for (int i = 1; i < matrices.length; i++) {
            out = STORE_FACTORY.copy(out.right(matrices[i].value));
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
    public static MatReal vertical(@Nonnull MatReal... matrices) {
        R064Store out = STORE_FACTORY.copy(matrices[0].value);
        for (int i = 1; i < matrices.length; i++) {
            out = STORE_FACTORY.copy(out.below(matrices[i].value));
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
        
        double[][] out = new double[L.getRows()][L.getCols()];
        double[][] currentL = L.value.toRawCopy2D();
        double[][] currentW = W.value.toRawCopy2D();

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
                double sum = b.get(i, k);
                for (int j = 0; j < i; j++) {
                    sum -= A.value.get(i, j) * out[j][k];
                }

                out[i][k] = sum / A.get(i, i);
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
                double sum = b.value.get(i, k);
                for (int j = A.getRows()-1; j >= i+1; j--) {
                    sum -= A.value.get(i, j) * out[j][k];
                }

                out[i][k] = sum / A.value.get(i, i);
            }
        }

        return new MatReal(out);
    }
}
