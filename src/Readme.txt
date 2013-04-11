Issues experienced:

*Realizing that the Matrix4f mul function assumes the input matrix is the right hand side of the multiplication (it didn't specify in the docs).
*Having to normalize the clipspace cube from (-1, 1) to (0, 1) in x, y, z space (not just x, y).
*Problems where our penumbra width being < 1.0 caused serious artifacts in PCSS.

We also started too late (what else is new) and are therefore turning this in a day late for a 10% penalty.