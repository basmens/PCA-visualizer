# About
This is just a small rough little one-day project to play around with PCA a bit because I had a really interesting lecture about it. Do not expect any good code from it or any javadoc, it was just a little play test thing.

# Setup
The folders to load the images from are hardcoded. So to get it working you have to replace the elements of the array with your own data. Now it's just pointing to the default minecraft textures on my laptop, which won't work for you (at least I strongly doubt it)

# Controls
* With the mouse button you can switch between different views. Current ones are stat for things like mean and variance of the data vectors, eigenvector for viewing the different eigenvectors in order, and the reconstruction view, where you can see how much of an image remains after it has been projected into the PCA subspace.
* With the space bar you can swap between options within each view: what stat to show in stat view, the eigenvector to show in eigenvector view, and the image to reconstruct in reconstruction view.
* In reconstruction view, you can use 0-9 keys to increase the amount of dimensions used by 10^k.