# Turborepo starter

This Turborepo starter is maintained by the Turborepo core team.

## Using this example

Run the following command:

```sh
npx create-turbo@latest
```

## What's inside?

This Turborepo includes the following packages/apps:

### Apps and Packages

- `docs`: a [Next.js](https://nextjs.org/) app
- `web`: another [Next.js](https://nextjs.org/) app
- `@repo/ui`: a stub React component library shared by both `web` and `docs` applications
- `@repo/eslint-config`: `eslint` configurations (includes `eslint-config-next` and `eslint-config-prettier`)
- `@repo/typescript-config`: `tsconfig.json`s used throughout the monorepo

Each package/app is 100% [TypeScript](https://www.typescriptlang.org/).

### Utilities

This Turborepo has some additional tools already setup for you:

- [TypeScript](https://www.typescriptlang.org/) for static type checking
- [ESLint](https://eslint.org/) for code linting
- [Prettier](https://prettier.io) for code formatting

### Build

To build all apps and packages, run the following command:

```
cd my-turborepo

# With [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation) installed (recommended)
turbo build

# Without [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation), use your package manager
npx turbo build
yarn dlx turbo build
pnpm exec turbo build
```

You can build a specific package by using a [filter](https://turborepo.com/docs/crafting-your-repository/running-tasks#using-filters):

```
# With [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation) installed (recommended)
turbo build --filter=docs

# Without [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation), use your package manager
npx turbo build --filter=docs
yarn exec turbo build --filter=docs
pnpm exec turbo build --filter=docs
```



## Deployment Model (Multi-Learner Lab)

The following diagram illustrates the distributed deployment model used in the ASMAS project.
Each AWS Academy Learner Lab represents an independent node and failure domain.


---
config:
  layout: elk
---
graph LR
    %% ==============================================================================
    %% STYLING DEFINITIONS
    %% ==============================================================================
    classDef controlNode fill:#eeeeee,stroke:#333,stroke-width:2px,stroke-dasharray: 5 5;
    classDef qaEnv fill:#e3f2fd,stroke:#1565c0,stroke-width:2px;
    classDef prodEnv fill:#fff3e0,stroke:#e65100,stroke-width:2px;
    classDef learnerLab fill:#ffffff,stroke:#666,stroke-width:1px,color:#333;
    classDef ec2Instance fill:#ff9900,stroke:#232f3e,color:#fff,stroke-width:1px;
    classDef dockerHost fill:#2496ed,stroke:#fff,color:#fff,stroke-width:1px;
    classDef db fill:#4caf50,stroke:#1b5e20,color:#fff,shape:cylinder;
    classDef external fill:#d1c4e9,stroke:#512da8,stroke-width:2px;

    %% ==============================================================================
    %% CONTROL PLANE (Orchestration)
    %% ==============================================================================
    subgraph CONTROL_PLANE [CONTROL LAB - Orchestration Node]
        direction TB
        subgraph CTRL_LAB [AWS Learner Lab: CONTROL]
            CTRL_EC2[EC2: Terraform Controller]:::controlNode
        end
    end

    %% ==============================================================================
    %% EXTERNAL SYSTEMS (Backup & Evaluation)
    %% ==============================================================================
    subgraph EXTERNAL_SYSTEMS [EXTERNAL / ON-PREMISE]
        BACKUP_NODE["Backup/Archival Node<br/>(Aggregation)"]:::external
        PROF_SERVER["Professor Server<br/>(On-Premises Audit)"]:::external
    end

    %% ==============================================================================
    %% QA ENVIRONMENT
    %% ==============================================================================
    subgraph QA_CLUSTER [QA ENVIRONMENT CLUSTER]
        direction TB

        %% QA LAB 1: EDGE
        subgraph QA_L1 [AWS Learner Lab: QA-EDGE]
            QA_E1[EC2 Runtime]:::ec2Instance
            subgraph QA_D1 [Docker Host]
                QA_GW(API Gateway)
                QA_AUTH(Auth Service)
            end
        end

        %% QA LAB 2: COMMAND
        subgraph QA_L2 [AWS Learner Lab: QA-COMMAND]
            QA_E2[EC2 Runtime]:::ec2Instance
            subgraph QA_D2 [Docker Host]
                QA_CMD_SVC(Survey Command Svc)
                QA_COL_SVC(Response Collector)
                QA_PG[(PostgreSQL)]:::db
            end
        end

        %% QA LAB 3: QUERY
        subgraph QA_L3 [AWS Learner Lab: QA-QUERY]
            QA_E3[EC2 Runtime]:::ec2Instance
            subgraph QA_D3 [Docker Host]
                QA_QRY_SVC(Survey Query Svc)
                QA_ANL_SVC(Analytics Svc)
                QA_REP_SVC(Reporting Svc)
                QA_MONGO[(MongoDB)]:::db
            end
        end

        %% QA LAB 4: EVENT
        subgraph QA_L4 [AWS Learner Lab: QA-EVENT]
            QA_E4[EC2 Runtime]:::ec2Instance
            subgraph QA_D4 [Docker Host]
                QA_PROC_SVC(Response Processor)
                QA_GAME_SVC(Gamification Svc)
                QA_NOT_SVC(Notification Svc)
            end
        end
    end

    %% ==============================================================================
    %% PROD ENVIRONMENT
    %% ==============================================================================
    subgraph PROD_CLUSTER [PROD ENVIRONMENT CLUSTER]
        direction TB

        %% PROD LAB 1: EDGE
        subgraph PR_L1 [AWS Learner Lab: PROD-EDGE]
            PR_E1[EC2 Runtime]:::ec2Instance
            subgraph PR_D1 [Docker Host]
                PR_GW(API Gateway)
                PR_AUTH(Auth Service)
            end
        end

        %% PROD LAB 2: COMMAND
        subgraph PR_L2 [AWS Learner Lab: PROD-COMMAND]
            PR_E2[EC2 Runtime]:::ec2Instance
            subgraph PR_D2 [Docker Host]
                PR_CMD_SVC(Survey Command Svc)
                PR_COL_SVC(Response Collector)
                PR_PG[(PostgreSQL)]:::db
            end
        end

        %% PROD LAB 3: QUERY
        subgraph PR_L3 [AWS Learner Lab: PROD-QUERY]
            PR_E3[EC2 Runtime]:::ec2Instance
            subgraph PR_D3 [Docker Host]
                PR_QRY_SVC(Survey Query Svc)
                PR_ANL_SVC(Analytics Svc)
                PR_REP_SVC(Reporting Svc)
                PR_MONGO[(MongoDB)]:::db
            end
        end

        %% PROD LAB 4: EVENT
        subgraph PR_L4 [AWS Learner Lab: PROD-EVENT]
            PR_E4[EC2 Runtime]:::ec2Instance
            subgraph PR_D4 [Docker Host]
                PR_PROC_SVC(Response Processor)
                PR_GAME_SVC(Gamification Svc)
                PR_NOT_SVC(Notification Svc)
            end
        end
    end

    %% ==============================================================================
    %% RELATIONSHIPS
    %% ==============================================================================

    %% 1. Provisioning (Terraform -> All EC2s)
    CTRL_EC2 -.->|SSH / Provisioning| QA_E1 & QA_E2 & QA_E3 & QA_E4
    CTRL_EC2 -.->|SSH / Provisioning| PR_E1 & PR_E2 & PR_E3 & PR_E4

    %% 2. QA Communication Flows (Public Internet)
    QA_GW -->|HTTPS / JWT| QA_CMD_SVC
    QA_GW -->|HTTPS / JWT| QA_QRY_SVC
    QA_CMD_SVC -.->|HTTP Async| QA_PROC_SVC
    QA_QRY_SVC -.->|HTTP Async| QA_ANL_SVC

    %% 3. PROD Communication Flows (Public Internet)
    PR_GW -->|HTTPS / JWT| PR_CMD_SVC
    PR_GW -->|HTTPS / JWT| PR_QRY_SVC
    PR_CMD_SVC -.->|HTTP Async| PR_PROC_SVC
    PR_QRY_SVC -.->|HTTP Async| PR_ANL_SVC

    %% 4. Backup Flows (Async)
    QA_PG & QA_MONGO -.->|Period Backup / JSON| BACKUP_NODE
    PR_PG & PR_MONGO -.->|Period Backup / JSON| BACKUP_NODE
    
    %% 5. Audit Flow
    BACKUP_NODE -->|Export Snapshot| PROF_SERVER

    %% ==============================================================================
    %% CLASS ASSIGNMENT
    %% ==============================================================================
    class QA_L1,QA_L2,QA_L3,QA_L4 learnerLab;
    class PR_L1,PR_L2,PR_L3,PR_L4 learnerLab;
    class QA_CLUSTER qaEnv;
    class PROD_CLUSTER prodEnv;
    class QA_D1,QA_D2,QA_D3,QA_D4,PR_D1,PR_D2,PR_D3,PR_D4 dockerHost;



### Develop

To develop all apps and packages, run the following command:

```
cd my-turborepo

# With [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation) installed (recommended)
turbo dev

# Without [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation), use your package manager
npx turbo dev
yarn exec turbo dev
pnpm exec turbo dev
```

You can develop a specific package by using a [filter](https://turborepo.com/docs/crafting-your-repository/running-tasks#using-filters):

```
# With [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation) installed (recommended)
turbo dev --filter=web

# Without [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation), use your package manager
npx turbo dev --filter=web
yarn exec turbo dev --filter=web
pnpm exec turbo dev --filter=web
```

### Remote Caching

> [!TIP]
> Vercel Remote Cache is free for all plans. Get started today at [vercel.com](https://vercel.com/signup?/signup?utm_source=remote-cache-sdk&utm_campaign=free_remote_cache).

Turborepo can use a technique known as [Remote Caching](https://turborepo.com/docs/core-concepts/remote-caching) to share cache artifacts across machines, enabling you to share build caches with your team and CI/CD pipelines.

By default, Turborepo will cache locally. To enable Remote Caching you will need an account with Vercel. If you don't have an account you can [create one](https://vercel.com/signup?utm_source=turborepo-examples), then enter the following commands:

```
cd my-turborepo

# With [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation) installed (recommended)
turbo login

# Without [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation), use your package manager
npx turbo login
yarn exec turbo login
pnpm exec turbo login
```

This will authenticate the Turborepo CLI with your [Vercel account](https://vercel.com/docs/concepts/personal-accounts/overview).

Next, you can link your Turborepo to your Remote Cache by running the following command from the root of your Turborepo:

```
# With [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation) installed (recommended)
turbo link

# Without [global `turbo`](https://turborepo.com/docs/getting-started/installation#global-installation), use your package manager
npx turbo link
yarn exec turbo link
pnpm exec turbo link
```

## Useful Links

Learn more about the power of Turborepo:

- [Tasks](https://turborepo.com/docs/crafting-your-repository/running-tasks)
- [Caching](https://turborepo.com/docs/crafting-your-repository/caching)
- [Remote Caching](https://turborepo.com/docs/core-concepts/remote-caching)
- [Filtering](https://turborepo.com/docs/crafting-your-repository/running-tasks#using-filters)
- [Configuration Options](https://turborepo.com/docs/reference/configuration)
- [CLI Usage](https://turborepo.com/docs/reference/command-line-reference)
