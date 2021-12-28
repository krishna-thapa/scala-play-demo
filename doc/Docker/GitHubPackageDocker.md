## Aim
- Use the Github action CI/CD tool to build the docker image once the `sbt clean compile test` command is done after merging the code to the master branch. 
- When the docker image is build, it has to be pushed to the Github package instead of Docker hub. 
- Have to pass the token or store the credentials somewhere in the github secrets page.
- Is there automatic version upgrading in github action while tagging a new docker image build?

## Resources 
- [Configuring Docker for use with GitHub Packages](https://docs.github.com/en/packages/guides/configuring-docker-for-use-with-github-packages)
- [Publishing to Github packages with Github actions](https://docs.github.com/en/actions/guides/publishing-docker-images#publishing-images-to-github-packages)