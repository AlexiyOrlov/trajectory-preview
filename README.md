Trajectory preview shows possible trajectory of shootable and throwable items.

If you want to make a plugin for Trajectory Preview, set up the API:

1. Add maven repository:
    ```
    maven {
        url 'https://mymavenrepo.com/repo/XXNTQu0VdMr93BjoOV1S/'
    }
    ```
2. Add API dependency:
    ```
    implementation('dev.buildtool:trajectory-preview:[version]:api')
    ```
3. Optionally, add a runtime dependency on Trajectory Preview to run your project with the mod:
    ```
    runtimeOnly fg.deobf('dev.buildtool:trajectory-preview:[version]:unobf')
    ```

Read the Javadoc and source of the basic plugin on how to create your own previews