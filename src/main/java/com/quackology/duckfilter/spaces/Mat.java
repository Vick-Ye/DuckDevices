package com.quackology.duckfilter.spaces;

/**
 * Interface for a matrix
 */
public interface Mat<T extends Mat<T>> extends Linear {

    /**
     * Gets the number of rows of the matrix
     * 
     * @return the number of rows of the matrix
     */
    public int getRows();

    /**
     * Gets the number of columns of the matrix
     * 
     * @return the number of columns of the matrix
     */
    public int getCols();

    /**
     * Gets the row of the matrix
     * 
     * @param row the row to get
     * @return the row of the matrix
     */
    public T getRow(int row);

    /**
     * Gets the column of the matrix
     * 
     * @param column
     * @return the column of the matrix
     */
    public T getCol(int column);

    /**
     * Gets the dimensions of the matrix
     * 
     * @return the dimensions of the matrix
     */
    public default int getDimensions() {
        return getRows() * getCols();
    };

    /**
     * Gets the transpose of the matrix
     * 
     * @return the transpose of the matrix
     */
    public T transpose();

    /**
     * Gets a submatrix of the matrix
     * 
     * @param row row to start the submatrix
     * @param column column to start the submatrix
     * @param height height of the submatrix from top down
     * @param width width of the submatrix from left to right
     * @return the submatrix of the matrix
     */
    public T subMat(int row, int column, int height, int width);

    /**
     * Adds the matrices together
     * 
     * @param matrix the matrix to add to the current matrix
     * @return a matrix that is the sum of the two matrices
     */
    public T add(T matrix);

    /**
     * Subtracts the matrices from each other
     * 
     * @param matrix the matrix to subtract from the current matrix
     * @return a matrix that is the difference of the two matrices
     */
    public T subtract(T matrix);

    /**
     * Multiplies the matrices together
     * 
     * @param matrix the matrix to multiply against the current matrix
     * @return a matrix that is the product of the two matrices
     */
    public T multiply(T matrix);

    /**
     * Multiplies the matrix by a scalar
     * 
     * @param scalar the scalar to multiply against the current matrix
     * @return a matrix that is the product of the matrix and the scalar
     */
    public T multiply(double scalar);
}; 
